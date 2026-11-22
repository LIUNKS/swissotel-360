package com.swissotel.swissotel360.billing.domain.exceptions;

public class InvoiceAlreadyIssuedException extends RuntimeException {
	public InvoiceAlreadyIssuedException() {
		super("Invoice already issued for this payment");
	}
}
