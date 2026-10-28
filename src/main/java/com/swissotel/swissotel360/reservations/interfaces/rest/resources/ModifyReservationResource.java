package com.swissotel.swissotel360.reservations.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description="Replacement room type and stay dates")
public record ModifyReservationResource(@NotNull UUID roomTypePublicId,@NotNull @FutureOrPresent LocalDate checkInDate,@NotNull LocalDate checkOutDate) { }
