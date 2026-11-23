package com.swissotel.swissotel360.servicerequests.domain.exceptions;

public class StayNotActiveForRequestException extends RuntimeException {
	public StayNotActiveForRequestException(java.util.UUID id) {
		super("Stay is not active: " + id);
	}
}
