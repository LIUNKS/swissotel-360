package com.swissotel.swissotel360.billing.domain.exceptions;

public class FolioClosedException extends RuntimeException {
	public FolioClosedException() {
		super("Folio is closed");
	}
}
