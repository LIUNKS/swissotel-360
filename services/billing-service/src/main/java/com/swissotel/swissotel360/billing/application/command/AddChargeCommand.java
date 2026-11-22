package com.swissotel.swissotel360.billing.application.command;

import java.math.BigDecimal;
import java.util.UUID;

public record AddChargeCommand(UUID folioId, String concept, BigDecimal amount, UUID serviceRequestId) {
}
