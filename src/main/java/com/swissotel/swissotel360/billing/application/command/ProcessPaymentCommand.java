package com.swissotel.swissotel360.billing.application.command;

import java.math.BigDecimal;
import java.util.UUID;

public record ProcessPaymentCommand(UUID folioId, String paymentMethodCode, BigDecimal amount) {
}
