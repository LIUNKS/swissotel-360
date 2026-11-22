package com.swissotel.swissotel360.billing.interfaces.events;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Consumer-owned integration event contract for completed service requests published to swissotel.events. */
public record ServiceRequestCompletedEventMessage(
		UUID requestPublicId,
		Long serviceRequestId,
		Long stayId,
		String serviceTypeCode,
		String serviceTypeName,
		BigDecimal chargeAmount,
		Instant completedAt) {
}
