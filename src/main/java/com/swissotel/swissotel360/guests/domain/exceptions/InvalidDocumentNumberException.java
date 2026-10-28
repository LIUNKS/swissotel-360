package com.swissotel.swissotel360.guests.domain.exceptions;

public class InvalidDocumentNumberException extends RuntimeException {
	public InvalidDocumentNumberException(String message) {
		super(message);
	}
}
