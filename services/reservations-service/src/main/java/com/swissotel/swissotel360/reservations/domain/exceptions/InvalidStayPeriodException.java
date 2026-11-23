package com.swissotel.swissotel360.reservations.domain.exceptions;

public class InvalidStayPeriodException extends RuntimeException {
	private final String reason;

	public InvalidStayPeriodException(String reason) {
		super(reason);
		this.reason = reason;
	}

	public String getReason() {
		return reason;
	}
}
