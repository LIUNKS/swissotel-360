package com.example.swissotel360.iam.interfaces.rest;

import com.example.swissotel360.iam.application.commandservices.UserCommandService;
import com.example.swissotel360.iam.interfaces.rest.resources.AuthenticatedUserResource;
import com.example.swissotel360.iam.interfaces.rest.resources.SignInResource;
import com.example.swissotel360.iam.interfaces.rest.resources.SignUpResource;
import com.example.swissotel360.iam.interfaces.rest.resources.UserResource;
import com.example.swissotel360.iam.interfaces.rest.transform.AuthenticatedUserResourceFromEntityAssembler;
import com.example.swissotel360.iam.interfaces.rest.transform.SignInCommandFromResourceAssembler;
import com.example.swissotel360.iam.interfaces.rest.transform.SignUpCommandFromResourceAssembler;
import com.example.swissotel360.iam.interfaces.rest.transform.UserResourceFromEntityAssembler;
import com.example.swissotel360.shared.interfaces.rest.transform.ResponseEntityAssembler;
import com.example.swissotel360.shared.interfaces.rest.transform.ErrorResponseAssembler;
import com.example.swissotel360.shared.application.result.Result;
import org.apache.commons.lang3.tuple.ImmutablePair;
import com.example.swissotel360.iam.domain.model.aggregates.User;
import com.example.swissotel360.iam.domain.model.commands.SignOutCommand;
import org.springframework.security.core.Authentication;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AuthenticationController
 * <p>
 *     This controller is responsible for handling authentication requests.
 *     It exposes two endpoints:
 *     <ul>
 *         <li>POST /api/v1/authentication/sign-in</li>
 *         <li>POST /api/v1/authentication/sign-up</li>
 *     </ul>
 * </p>
 */
@RestController
@RequestMapping(value = "/api/v1/authentication", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Authentication", description = "Authentication and user registration endpoints")
public class AuthenticationController {
    private final UserCommandService userCommandService;

    public AuthenticationController(UserCommandService userCommandService) {
        this.userCommandService = userCommandService;
    }

    /**
     * Handles the sign-in request.
     * @param signInResource the sign-in request body with username and password.
     * @return the authenticated user resource with JWT token.
     */
    @PostMapping("/sign-in")
    @Operation(
        summary = "User sign-in",
        description = "Authenticates a user with provided credentials and returns JWT token for subsequent requests."
    )
    @ApiResponses(value = {
            @ApiResponse(
                responseCode = "200",
                description = "User authenticated successfully",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AuthenticatedUserResource.class)
                )
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid credentials or malformed request",
                content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                responseCode = "404",
                description = "User not found with provided username",
                content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<?> signIn(@RequestBody SignInResource signInResource) {
        var signInCommand = SignInCommandFromResourceAssembler.toCommandFromResource(signInResource);
        var result = userCommandService.handle(signInCommand);
        
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                auth -> AuthenticatedUserResourceFromEntityAssembler.toResourceFromEntity(auth.getLeft()),
                auth -> {
                    ResponseCookie cookie = ResponseCookie.from("token", auth.getRight())
                            .httpOnly(true)
                            .path("/")
                            .maxAge(24 * 60 * 60)
                            .build();
                    HttpHeaders headers = new HttpHeaders();
                    headers.add(HttpHeaders.SET_COOKIE, cookie.toString());
                    return headers;
                },
                HttpStatus.OK
        );
    }

    /**
     * Handles the sign-up request.
     * @param signUpResource the sign-up request body with username, password, and roles.
     * @return the created user resource with assigned roles.
     */
    @PostMapping("/sign-up")
    @Operation(
        summary = "User registration",
        description = "Creates a new user account with provided credentials and assigns specified roles."
    )
    @ApiResponses(value = {
            @ApiResponse(
                responseCode = "201",
                description = "User created successfully",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserResource.class)
                )
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid input data or username already exists",
                content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                responseCode = "409",
                description = "Conflict - username already taken",
                content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<?> signUp(@RequestBody SignUpResource signUpResource) {
        var signUpCommand = SignUpCommandFromResourceAssembler.toCommandFromResource(signUpResource);
        var result = userCommandService.handle(signUpCommand);
        
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                UserResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.CREATED
        );
    }

    /**
     * Handles the sign-out request.
     * @return a response indicating successful sign-out.
     */
    @PostMapping("/sign-out")
    @Operation(
        summary = "User sign-out",
        description = "Logs out a user by clearing the authentication token cookie."
    )
    @ApiResponses(value = {
            @ApiResponse(
                responseCode = "200",
                description = "User signed out successfully"
            )
    })
    public ResponseEntity<?> signOut(Authentication authentication) {
        if (authentication != null && authentication.getName() != null) {
            var command = new SignOutCommand(authentication.getName());
            userCommandService.handle(command);
        }
        
        ResponseCookie cookie = ResponseCookie.from("token", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .build();
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.ok().headers(headers).body("User signed out successfully");
    }
}
