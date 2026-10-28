package com.swissotel.swissotel360.servicerequests.domain.exceptions;

public class InvalidServiceRequestTransitionException extends RuntimeException {
	public InvalidServiceRequestTransitionException(String from, String to) {
		super("Invalid service request transition: " + from + " -> " + to);
	}
}
