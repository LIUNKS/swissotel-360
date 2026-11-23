package com.swissotel.swissotel360.servicerequests.domain.events;

import com.swissotel.swissotel360.servicerequests.domain.model.ServiceRequestId;
import java.math.BigDecimal;

/** chargeAmount is null when the service carries no charge. */
public record ServiceRequestCompleted(ServiceRequestId id, Long stayId, String serviceTypeCode,
		String serviceTypeName, BigDecimal chargeAmount) {
}
