package com.swissotel.swissotel360.servicerequests.domain.exceptions;

public class ServiceRequestStayNotFoundException extends RuntimeException {
	public ServiceRequestStayNotFoundException(java.util.UUID id) {
		super("Stay not found: " + id);
	}
}
