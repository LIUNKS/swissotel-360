package com.swissotel.swissotel360.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Data required for user registration")
public record SignUpResource(
                @Schema(description = "Full name of the user", example = "John Doe") @NotBlank @Size(max = 120) String fullName,

                @Schema(description = "Email of the user, used for login", example = "john.doe@example.com") @NotBlank @Email @Size(max = 150) String email,

                @Schema(description = "Password for the account", example = "secretPassword123") @NotBlank @Size(min = 6, max = 120) String password,

                @Schema(description = "Role to assign", example = "ROLE_GUEST") @NotBlank String role) {
}
