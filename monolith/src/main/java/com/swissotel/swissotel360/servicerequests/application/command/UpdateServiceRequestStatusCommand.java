package com.swissotel.swissotel360.servicerequests.application.command;

import com.swissotel.swissotel360.servicerequests.domain.model.ServiceRequestStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.UUID;

public record UpdateServiceRequestStatusCommand(@NotNull UUID requestPublicId, @NotNull ServiceRequestStatus status,
		@PositiveOrZero BigDecimal chargeAmount) {
}
