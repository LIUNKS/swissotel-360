package com.swissotel.swissotel360.stays.interfaces.rest.resources;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Digital key; the access code is only returned when the key is issued")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record DigitalKeyResource(Long keyId, @Schema(example = "K7M2Q9XA4TBD") String accessCode,
		@Schema(example = "active") String status) {
}
