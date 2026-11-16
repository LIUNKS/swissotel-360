package com.swissotel.swissotel360.rooms.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Room rate creation payload")
public record CreateRateResource(@Schema(example = "420.00") @NotNull @PositiveOrZero BigDecimal price,
		@Schema(example = "high-season") @Size(max = 30) String season,
		@Schema(example = "2026-06-01") @NotNull LocalDate validFrom,
		@Schema(example = "2026-08-31") LocalDate validTo) {
}
