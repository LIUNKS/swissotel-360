package com.swissotel.swissotel360.servicerequests.domain.exceptions;

public class ServiceTypeNotFoundException extends RuntimeException {
	public ServiceTypeNotFoundException(String code) {
		super("Service type not found: " + code);
	}
}
