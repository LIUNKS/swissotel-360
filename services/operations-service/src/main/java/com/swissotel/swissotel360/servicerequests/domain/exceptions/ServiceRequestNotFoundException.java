package com.swissotel.swissotel360.servicerequests.domain.exceptions;

public class ServiceRequestNotFoundException extends RuntimeException {
	public ServiceRequestNotFoundException(java.util.UUID id) {
		super("Service request not found: " + id);
	}
}
