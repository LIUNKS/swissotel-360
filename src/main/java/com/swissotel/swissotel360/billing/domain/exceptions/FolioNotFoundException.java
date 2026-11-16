package com.swissotel.swissotel360.billing.domain.exceptions;

public class FolioNotFoundException extends RuntimeException {
	public FolioNotFoundException(java.util.UUID id) {
		super("Folio not found: " + id);
	}
}
