package com.swissotel.swissotel360.stays.application.command;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record CheckInCommand(@NotBlank String reservationCode, @NotBlank String documentType,
		@NotBlank String documentNumber, UUID roomPublicId) {
}
