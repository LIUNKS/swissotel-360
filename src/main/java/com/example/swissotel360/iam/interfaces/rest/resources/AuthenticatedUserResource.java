package com.example.swissotel360.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Resource returned after successful authentication.
 *
 * <p>Contains the authenticated user identifier and username.</p>
 * <p>The JWT token is securely transmitted via HttpOnly Cookie.</p>
 */
@Schema(
    name = "AuthenticatedUserResponse",
    description = "Authenticated user information. The JWT token is securely transmitted via HttpOnly Cookie.",    
    example = "{\"id\": 1, \"username\": \"john.doe\"}"
)
public record AuthenticatedUserResource(
    @Schema(description = "User unique identifier", example = "1")
    Long id,

    @Schema(description = "User username", example = "john.doe")
    String username
) {
}
