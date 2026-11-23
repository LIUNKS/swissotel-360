package com.swissotel.swissotel360.iam.domain.model.queries;

import jakarta.validation.constraints.NotNull;

public record GetUserByIdQuery(@NotNull Long userId) {

}