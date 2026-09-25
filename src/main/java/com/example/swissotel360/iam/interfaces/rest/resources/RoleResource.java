package com.example.swissotel360.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Resource that represents an IAM role exposed by the REST API.
 */
@Schema(
    name = "RoleResponse",
    description = "Role information response",
    example = "{\"id\": 1, \"name\": \"ROLE_GUEST\"}"
)
public record RoleResource(
    @Schema(description = "Role unique identifier", example = "1")
    Long id,

    @Schema(description = "Role name", example = "ROLE_GUEST", allowableValues = {"ROLE_GUEST", "ROLE_RECEPTIONIST", "ROLE_MANAGER"})
    String name
) {
}
