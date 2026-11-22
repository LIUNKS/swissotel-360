package com.swissotel.swissotel360.billing.domain.exceptions;

public class FolioAlreadyPaidException extends RuntimeException {
	public FolioAlreadyPaidException() {
		super("Folio already has a payment");
	}
}
