package com.swissotel.swissotel360.stays.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

@Schema(description = "Check-in request")
public record CheckInStayResource(
		@Schema(example = "SW26-7H3KQ9XA") @NotBlank String reservationCode,
		@Schema(description = "Document type code of the guest", example = "DNI") @NotBlank String documentType,
		@Schema(example = "12345678") @NotBlank String documentNumber,
		@Schema(description = "Optional public id of the room to assign; an available room of the reserved type is chosen when omitted") UUID roomId) {
}
