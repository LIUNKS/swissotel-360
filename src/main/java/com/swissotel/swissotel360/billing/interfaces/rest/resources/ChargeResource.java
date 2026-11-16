package com.swissotel.swissotel360.billing.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Folio line item")
public record ChargeResource(Long chargeId, String concept, BigDecimal amount, Instant chargedAt,
		UUID serviceRequestId) {
}
