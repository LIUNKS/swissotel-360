package com.swissotel.swissotel360.guests.domain.exceptions;

public class DuplicateGuestException extends RuntimeException {
	private final Long documentTypeId;
	private final String documentNumber;

	public DuplicateGuestException(Long documentTypeId, String documentNumber) {
		super("A guest with this document already exists");
		this.documentTypeId = documentTypeId;
		this.documentNumber = documentNumber;
	}

	public Long getDocumentTypeId() {
		return documentTypeId;
	}

	public String getDocumentNumber() {
		return documentNumber;
	}
}
