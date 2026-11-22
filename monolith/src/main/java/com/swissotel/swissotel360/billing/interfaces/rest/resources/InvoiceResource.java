package com.swissotel.swissotel360.billing.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Issued receipt or invoice")
public record InvoiceResource(UUID invoiceId, @Schema(example = "receipt") String documentType,
		@Schema(example = "B001") String series, @Schema(example = "00000001") String number,
		BigDecimal totalAmount, Instant issuedAt) {
}
