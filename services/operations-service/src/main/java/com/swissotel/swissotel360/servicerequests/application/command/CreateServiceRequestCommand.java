package com.swissotel.swissotel360.servicerequests.application.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateServiceRequestCommand(@NotNull UUID stayPublicId, @NotBlank String serviceTypeCode,
		String description) {
}
