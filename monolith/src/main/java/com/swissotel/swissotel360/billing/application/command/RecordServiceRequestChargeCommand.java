package com.swissotel.swissotel360.billing.application.command;

import java.math.BigDecimal;

/** Internal ids; originates from the service request completed integration event. */
public record RecordServiceRequestChargeCommand(Long stayId, Long serviceRequestId, String concept,
		BigDecimal amount) {
}
