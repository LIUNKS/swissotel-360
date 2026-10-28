package com.swissotel.swissotel360.billing.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Adds a manual charge to an open folio")
public record AddChargeResource(@NotBlank @Size(max = 120) @Schema(example = "Minibar") String concept,
		@NotNull @PositiveOrZero @Digits(integer = 8, fraction = 2) @Schema(example = "35.00") BigDecimal amount,
		@Schema(description = "Optional related service request") UUID serviceRequestId) {
}