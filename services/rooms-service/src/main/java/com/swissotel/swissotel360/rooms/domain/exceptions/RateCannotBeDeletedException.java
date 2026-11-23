package com.swissotel.swissotel360.rooms.domain.exceptions;

import java.util.UUID;

public class RateCannotBeDeletedException extends RuntimeException {
	public RateCannotBeDeletedException(UUID publicId) {
		super("Effective or past rates cannot be deleted: " + publicId);
	}
}
