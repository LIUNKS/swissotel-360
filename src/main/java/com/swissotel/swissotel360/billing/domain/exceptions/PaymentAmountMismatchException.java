package com.swissotel.swissotel360.billing.domain.exceptions;

public class PaymentAmountMismatchException extends RuntimeException {
	public PaymentAmountMismatchException() {
		super("Payment amount must equal the folio total");
	}
}
