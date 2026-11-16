package com.swissotel.swissotel360.billing.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Pays the full balance of an open folio")
public record CreatePaymentResource(@NotNull UUID folioId,
		@NotBlank @Size(max = 30) @Schema(example = "cash") String paymentMethodCode,
		@NotNull @Positive @Digits(integer = 8, fraction = 2) @Schema(example = "120.50") BigDecimal amount) {
}