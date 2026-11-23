package com.swissotel.swissotel360.guests.domain.exceptions;

public class DuplicateGuestException extends RuntimeException {
	private final String documentTypeCode;
	private final String documentNumber;

	public DuplicateGuestException(String documentTypeCode, String documentNumber) {
		super("A guest with this document already exists");
		this.documentTypeCode = documentTypeCode;
		this.documentNumber = documentNumber;
	}

	public String getDocumentTypeCode() {
		return documentTypeCode;
	}

	public String getDocumentNumber() {
		return documentNumber;
	}
}
