package com.swissotel.swissotel360.billing.domain.exceptions;

public class ChargeServiceRequestNotFoundException extends RuntimeException {
	public ChargeServiceRequestNotFoundException(java.util.UUID id) {
		super("Service request not found for charge: " + id);
	}
}
