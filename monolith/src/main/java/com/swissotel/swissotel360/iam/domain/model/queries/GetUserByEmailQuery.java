package com.swissotel.swissotel360.iam.domain.model.queries;

import jakarta.validation.constraints.NotBlank;

public record GetUserByEmailQuery(@NotBlank String email) {
}
