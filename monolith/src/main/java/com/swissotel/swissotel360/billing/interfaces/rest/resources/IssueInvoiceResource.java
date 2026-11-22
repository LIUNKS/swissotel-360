package com.swissotel.swissotel360.billing.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.UUID;

@Schema(description = "Issues a receipt or invoice for a processed payment")
public record IssueInvoiceResource(@NotNull UUID paymentId,
		@NotBlank @Pattern(regexp = "receipt|invoice") @Schema(example = "receipt", allowableValues = { "receipt",
				"invoice" }) String documentType) {
}