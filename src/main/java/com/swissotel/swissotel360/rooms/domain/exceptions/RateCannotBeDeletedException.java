package com.swissotel.swissotel360.rooms.domain.exceptions;

public class RateCannotBeDeletedException extends RuntimeException {
	public RateCannotBeDeletedException(Long id) {
		super("Effective or past rates cannot be deleted: " + id);
	}
}
