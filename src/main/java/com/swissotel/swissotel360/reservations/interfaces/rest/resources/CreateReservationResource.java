package com.swissotel.swissotel360.reservations.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Request to create a reservation")
public record CreateReservationResource(
		@Schema(example = "4fd4f43a-eeb4-4c29-a67a-92cc88ea8dae") UUID guestPublicId,
		@NotNull @Schema(example = "8c454ab8-fd10-42a0-9a7c-1c1496e6dabd") UUID roomTypePublicId,
		@NotNull @FutureOrPresent LocalDate checkInDate,
		@NotNull LocalDate checkOutDate,
		@NotBlank @Pattern(regexp = "immediate|deferred") String paymentType) {
}
