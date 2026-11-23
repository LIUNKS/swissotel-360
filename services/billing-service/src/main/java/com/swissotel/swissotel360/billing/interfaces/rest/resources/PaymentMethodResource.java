package com.swissotel.swissotel360.billing.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Accepted payment method")
public record PaymentMethodResource(@Schema(example = "cash") String code, @Schema(example = "Efectivo") String name) {
}
