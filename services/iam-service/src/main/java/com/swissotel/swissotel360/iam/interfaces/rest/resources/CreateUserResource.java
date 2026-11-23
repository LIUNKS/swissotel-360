package com.swissotel.swissotel360.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Data required for staff user creation by an administrator")
public record CreateUserResource(
		@Schema(description = "Full name of the staff member", example = "Carlos Receptionist") @NotBlank @Size(max = 120) String fullName,

		@Schema(description = "Staff email address, used for login", example = "carlos.staff@swissotel.com") @NotBlank @Email @Size(max = 150) String email,

		@Schema(description = "Initial password for the account", example = "TempPassword123!") @NotBlank @Size(min = 6, max = 120) String password,

		@Schema(description = "Role to assign to the staff user", example = "ROLE_RECEPTIONIST") @NotBlank String role) {
}
