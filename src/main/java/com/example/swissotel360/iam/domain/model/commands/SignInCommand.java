package com.example.swissotel360.iam.domain.model.commands;

import jakarta.validation.constraints.NotBlank;

public record SignInCommand(
        @NotBlank String email, 
        @NotBlank String password
) {
}
