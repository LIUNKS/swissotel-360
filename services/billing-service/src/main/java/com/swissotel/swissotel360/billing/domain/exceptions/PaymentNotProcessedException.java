package com.swissotel.swissotel360.billing.domain.exceptions;

public class PaymentNotProcessedException extends RuntimeException {
	public PaymentNotProcessedException() {
		super("Payment is not processed");
	}
}
