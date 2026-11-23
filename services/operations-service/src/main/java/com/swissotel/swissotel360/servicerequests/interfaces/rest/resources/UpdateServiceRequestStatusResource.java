package com.swissotel.swissotel360.servicerequests.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

@Schema(description = "Moves a service request forward; chargeAmount is only used when completing")
public record UpdateServiceRequestStatusResource(
		@NotBlank @Schema(example = "completed", allowableValues = { "in_progress", "completed" }) String status,
		@PositiveOrZero @Schema(example = "45.00") BigDecimal chargeAmount) {
}
