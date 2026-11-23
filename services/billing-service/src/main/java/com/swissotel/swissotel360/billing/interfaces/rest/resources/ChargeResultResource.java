package com.swissotel.swissotel360.billing.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Charge added to a folio")
public record ChargeResultResource(UUID chargeId, String concept, BigDecimal amount, Instant chargedAt) {
}
