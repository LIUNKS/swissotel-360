package com.swissotel.swissotel360.billing.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Schema(description = "Folio with its charges")
public record FolioSummaryResource(UUID folioId, String status, List<ChargeResource> charges,
		BigDecimal totalAmount) {
}
