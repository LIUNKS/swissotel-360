package com.swissotel.swissotel360.iam.interfaces.rest;

import com.swissotel.swissotel360.iam.application.queryservices.UserQueryService;

import com.swissotel.swissotel360.iam.application.commandservices.UserCommandService;
import com.swissotel.swissotel360.iam.domain.model.queries.GetUserByIdQuery;
import com.swissotel.swissotel360.iam.interfaces.rest.resources.CreateUserResource;
import com.swissotel.swissotel360.iam.interfaces.rest.resources.UserResource;
import com.swissotel.swissotel360.iam.interfaces.rest.transform.CreateUserCommandFromResourceAssembler;
import com.swissotel.swissotel360.iam.interfaces.rest.transform.UserResourceFromEntityAssembler;
import com.swissotel.swissotel360.shared.application.result.Result;
import com.swissotel.swissotel360.shared.interfaces.rest.resources.ErrorResource;
import com.swissotel.swissotel360.shared.interfaces.rest.transform.ErrorResponseAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller that exposes IAM user resources.
 */
@RestController
@RequestMapping(value = "/api/v1/users", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Users", description = "User management endpoints")
public class UsersController {
    private final UserQueryService userQueryService;
    private final UserCommandService userCommandService;

    public UsersController(UserQueryService userQueryService, UserCommandService userCommandService) {
        this.userQueryService = userQueryService;
        this.userCommandService = userCommandService;
    }

    /**
     * Creates a staff user with a specific role.
     * Accessible only to users with ROLE_SUPER_ADMIN or ROLE_MANAGER.
     *
     * @param resource staff user data
     * @return created user resource
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MANAGER')")
    @Operation(summary = "Create staff user", description = "Creates a staff user with an assigned role. Requires SUPER_ADMIN or MANAGER role.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Staff user created successfully", content = @Content(schema = @Schema(implementation = UserResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input or validation failure", content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_ADMIN or MANAGER role", content = @Content),
            @ApiResponse(responseCode = "409", description = "User email already exists", content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    })
    public ResponseEntity<?> createUser(@Valid @RequestBody CreateUserResource resource) {
        var command = CreateUserCommandFromResourceAssembler.toCommandFromResource(resource);
        var result = userCommandService.handle(command);

        return switch (result) {
            case Result.Success(var user) ->
                new ResponseEntity<>(UserResourceFromEntityAssembler.toResourceFromEntity(user), HttpStatus.CREATED);
            case Result.Failure(var error) ->
                ErrorResponseAssembler.toErrorResponseFromApplicationError(error);
        };
    }

    /**
     * Retrieves a user by identifier.
     *
     * @param userId user identifier
     * @return user resource when found
     * @see UserResource
     */
    @GetMapping(value = "/{userId}")
    @Operation(summary = "Get user by ID", description = "Retrieves a specific user's information by unique identifier.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User retrieved successfully", content = @Content(schema = @Schema(implementation = UserResource.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token required or invalid", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions", content = @Content),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    })
    public ResponseEntity<UserResource> getUserById(
            @PathVariable @Parameter(description = "Unique user identifier", example = "1", required = true) Long userId) {
        var getUserByIdQuery = new GetUserByIdQuery(userId);
        var user = userQueryService.handle(getUserByIdQuery);
        if (user.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var userResource = UserResourceFromEntityAssembler.toResourceFromEntity(user.get());
        return ResponseEntity.ok(userResource);
    }
}
