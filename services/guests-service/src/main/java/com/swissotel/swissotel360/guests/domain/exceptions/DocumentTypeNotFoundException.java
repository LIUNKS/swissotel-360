package com.swissotel.swissotel360.guests.domain.exceptions;

public class DocumentTypeNotFoundException extends RuntimeException {
	public DocumentTypeNotFoundException(String code) {
		super("Document type not found: " + code);
	}
}
