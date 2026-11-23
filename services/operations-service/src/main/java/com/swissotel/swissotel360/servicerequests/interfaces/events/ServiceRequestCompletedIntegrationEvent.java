package com.swissotel.swissotel360.servicerequests.interfaces.events;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Public event emitted after a service request completes and commits. chargeAmount is null when nothing is billable. */
public record ServiceRequestCompletedIntegrationEvent(UUID requestPublicId, Long serviceRequestId, Long stayId,
		String serviceTypeCode, String serviceTypeName, BigDecimal chargeAmount, Instant completedAt) {
}
