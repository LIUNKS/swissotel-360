package com.swissotel.swissotel360.stays.domain.exceptions;

public class GuestDocumentMismatchException extends RuntimeException {
	public GuestDocumentMismatchException() {
		super("Guest document does not match the reservation guest");
	}
}
