package com.swissotel.swissotel360.rooms.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Schema(description = "End date for an open room rate")
public record CloseRateResource(@Schema(example = "2026-05-31") @NotNull LocalDate validTo) {
}
