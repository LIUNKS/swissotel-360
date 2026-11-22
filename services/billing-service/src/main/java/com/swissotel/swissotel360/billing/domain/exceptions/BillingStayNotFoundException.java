package com.swissotel.swissotel360.billing.domain.exceptions;

public class BillingStayNotFoundException extends RuntimeException {
	public BillingStayNotFoundException(java.util.UUID id) {
		super("Stay not found: " + id);
	}
}
