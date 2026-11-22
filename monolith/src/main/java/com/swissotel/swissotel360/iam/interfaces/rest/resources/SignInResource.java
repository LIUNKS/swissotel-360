package com.swissotel.swissotel360.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Data required for user login")
public record SignInResource(
		@Schema(description = "Registered email", example = "john.doe@example.com") @NotBlank @Email String email,

		@Schema(description = "User password", example = "secretPassword123") @NotBlank String password) {
}
