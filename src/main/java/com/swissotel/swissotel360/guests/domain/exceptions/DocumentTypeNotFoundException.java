package com.swissotel.swissotel360.guests.domain.exceptions;

public class DocumentTypeNotFoundException extends RuntimeException {
	public DocumentTypeNotFoundException(Long id) {
		super("Document type not found: " + id);
	}
}
