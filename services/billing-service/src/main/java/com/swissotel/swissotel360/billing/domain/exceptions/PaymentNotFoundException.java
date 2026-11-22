package com.swissotel.swissotel360.billing.domain.exceptions;

public class PaymentNotFoundException extends RuntimeException {
	public PaymentNotFoundException(java.util.UUID id) {
		super("Payment not found: " + id);
	}
}
