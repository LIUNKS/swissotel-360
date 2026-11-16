package com.swissotel.swissotel360.billing.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Guest folio")
public record FolioResource(UUID folioId, UUID stayId, @Schema(example = "open") String status,
		@Schema(example = "120.50") BigDecimal totalAmount) {
}
