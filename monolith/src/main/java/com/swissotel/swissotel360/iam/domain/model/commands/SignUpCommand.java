package com.swissotel.swissotel360.iam.domain.model.commands;

import com.swissotel.swissotel360.iam.domain.model.valueobjects.Roles;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SignUpCommand(
		@NotBlank @Size(max = 120) String fullName,
		@NotBlank @Size(max = 150) String email,
		@NotBlank @Size(min = 6, max = 120) String password,
		@NotNull Roles role) {
}
