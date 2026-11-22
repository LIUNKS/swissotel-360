package com.swissotel.swissotel360.billing.domain.exceptions;

public class PaymentMethodNotFoundException extends RuntimeException {
	public PaymentMethodNotFoundException(String code) {
		super("Payment method not found: " + code);
	}
}
