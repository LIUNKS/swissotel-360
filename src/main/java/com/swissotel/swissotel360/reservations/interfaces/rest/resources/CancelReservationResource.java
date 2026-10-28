package com.swissotel.swissotel360.reservations.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Optional cancellation note")
public record CancelReservationResource(@Size(max = 200) String note) {
}
