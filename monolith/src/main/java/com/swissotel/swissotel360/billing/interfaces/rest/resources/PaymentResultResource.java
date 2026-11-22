package com.swissotel.swissotel360.billing.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Payment outcome")
public record PaymentResultResource(UUID paymentId, @Schema(example = "processed") String status,
		String gatewayReference, Instant paidAt) {
}
