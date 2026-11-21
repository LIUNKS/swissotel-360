package com.swissotel.swissotel360.iam.interfaces.rest;

import com.swissotel.swissotel360.iam.application.commandservices.UserCommandService;
import com.swissotel.swissotel360.iam.application.queryservices.UserQueryService;
import com.swissotel.swissotel360.iam.domain.model.queries.GetUserByEmailQuery;
import com.swissotel.swissotel360.iam.interfaces.rest.resources.AuthenticatedUserResource;
import com.swissotel.swissotel360.iam.interfaces.rest.resources.SignInResource;
import com.swissotel.swissotel360.iam.interfaces.rest.resources.SignUpResource;
import com.swissotel.swissotel360.iam.interfaces.rest.resources.UserResource;
import com.swissotel.swissotel360.iam.interfaces.rest.transform.AuthenticatedUserResourceFromEntityAssembler;
import com.swissotel.swissotel360.iam.interfaces.rest.transform.SignInCommandFromResourceAssembler;
import com.swissotel.swissotel360.iam.interfaces.rest.transform.SignUpCommandFromResourceAssembler;
import com.swissotel.swissotel360.iam.interfaces.rest.transform.UserResourceFromEntityAssembler;
import com.swissotel.swissotel360.shared.application.result.ApplicationError;
import com.swissotel.swissotel360.shared.application.result.Result;
import com.swissotel.swissotel360.shared.interfaces.rest.resources.ErrorResource;
import com.swissotel.swissotel360.shared.interfaces.rest.transform.ErrorResponseAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v1/authentication", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Authentication", description = "Authentication Endpoints")
public class AuthenticationController {

	private final UserCommandService userCommandService;
	private final UserQueryService userQueryService;
	private final boolean cookieSecure;
	private final String cookieSameSite;
	private final long expirationDays;

	public AuthenticationController(
			UserCommandService userCommandService,
			UserQueryService userQueryService,
			@Value("${authorization.cookie.secure:false}") boolean cookieSecure,
			@Value("${authorization.cookie.same-site:Lax}") String cookieSameSite,
			@Value("${authorization.jwt.expiration.days:7}") long expirationDays) {
		this.userCommandService = userCommandService;
		this.userQueryService = userQueryService;
		this.cookieSecure = cookieSecure;
		this.cookieSameSite = cookieSameSite;
		this.expirationDays = expirationDays;
	}

	@Operation(summary = "Sign up a new user", description = "Registers a new user in the system with the specified role")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "User successfully registered", content = @Content(schema = @Schema(implementation = UserResource.class))),
			@ApiResponse(responseCode = "400", description = "Invalid input or validation failure", content = @Content(schema = @Schema(implementation = ErrorResource.class))),
			@ApiResponse(responseCode = "409", description = "User email already exists", content = @Content(schema = @Schema(implementation = ErrorResource.class)))
	})
	@PostMapping("/sign-up")
	public ResponseEntity<?> signUp(@Valid @RequestBody SignUpResource resource) {
		var command = SignUpCommandFromResourceAssembler.toCommandFromResource(resource);
		var result = userCommandService.handle(command);

		return switch (result) {
			case Result.Success(var user) ->
				new ResponseEntity<>(UserResourceFromEntityAssembler.toResourceFromEntity(user), HttpStatus.CREATED);
			case Result.Failure(var error) ->
				ErrorResponseAssembler.toErrorResponseFromApplicationError(error);
		};
	}

	@Operation(summary = "Sign in user", description = "Authenticates a user and returns a session cookie and user details")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successfully authenticated", content = @Content(schema = @Schema(implementation = AuthenticatedUserResource.class))),
			@ApiResponse(responseCode = "400", description = "Invalid input or validation failure", content = @Content(schema = @Schema(implementation = ErrorResource.class))),
			@ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content(schema = @Schema(implementation = ErrorResource.class)))
	})
	@PostMapping("/sign-in")
	public ResponseEntity<?> signIn(@Valid @RequestBody SignInResource resource) {
		var command = SignInCommandFromResourceAssembler.toCommandFromResource(resource);
		var result = userCommandService.handle(command);

		return switch (result) {
			case Result.Success(var pair) -> {
				var user = pair.getLeft();
				var token = pair.getRight();

				ResponseCookie cookie = buildSessionCookie(token, expirationDays * 24 * 60 * 60);

				yield ResponseEntity.ok()
						.header(HttpHeaders.SET_COOKIE, cookie.toString())
						.body(AuthenticatedUserResourceFromEntityAssembler.toResourceFromEntity(user));
			}
			case Result.Failure(var error) ->
				ErrorResponseAssembler.toErrorResponseFromApplicationError(error);
		};
	}

	@Operation(summary = "Sign out user", description = "Clears the session cookie")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "204", description = "Session cookie cleared")
	})
	@PostMapping("/sign-out")
	public ResponseEntity<Void> signOut() {
		ResponseCookie cookie = buildSessionCookie("", 0);
		return ResponseEntity.noContent()
				.header(HttpHeaders.SET_COOKIE, cookie.toString())
				.build();
	}

	@Operation(summary = "Get current authenticated user", description = "Returns profile details for the authenticated session user", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Authenticated user details", content = @Content(schema = @Schema(implementation = AuthenticatedUserResource.class))),
			@ApiResponse(responseCode = "401", description = "Authentication required", content = @Content(schema = @Schema(implementation = ErrorResource.class)))
	})
	@GetMapping("/me")
	public ResponseEntity<?> me(Authentication authentication) {
		if (authentication == null || !authentication.isAuthenticated() || authentication.getName() == null) {
			return ErrorResponseAssembler.toErrorResponseFromApplicationError(
					ApplicationError.unauthorized("Authentication required"));
		}
		var user = userQueryService.handle(new GetUserByEmailQuery(authentication.getName()));
		if (user.isEmpty()) {
			return ErrorResponseAssembler.toErrorResponseFromApplicationError(
					ApplicationError.unauthorized("Authenticated user not found"));
		}
		return ResponseEntity.ok(AuthenticatedUserResourceFromEntityAssembler.toResourceFromEntity(user.get()));
	}

	private ResponseCookie buildSessionCookie(String value, long maxAgeSeconds) {
		return ResponseCookie.from("token", value)
				.httpOnly(true)
				.secure(cookieSecure)
				.sameSite(cookieSameSite)
				.path("/")
				.maxAge(maxAgeSeconds)
				.build();
	}
}
