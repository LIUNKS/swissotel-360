package com.swissotel.swissotel360.guests.interfaces.rest.resources;

import com.swissotel.swissotel360.guests.domain.model.valueobjects.DocumentType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "DocumentType")
public record DocumentTypeResource(
		@Schema(example = "1") Long id,
		@Schema(example = "DNI") String code,
		@Schema(example = "Documento Nacional de Identidad") String name,
		@Schema(example = "false") boolean hasCheckDigit) {
	public static DocumentTypeResource from(DocumentType type) {
		return new DocumentTypeResource(type.id(), type.code(), type.name(), type.hasCheckDigit());
	}
}
