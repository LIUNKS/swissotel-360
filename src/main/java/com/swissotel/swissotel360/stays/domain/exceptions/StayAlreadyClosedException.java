package com.swissotel.swissotel360.stays.domain.exceptions;

import java.util.UUID;

public class StayAlreadyClosedException extends RuntimeException {
	public StayAlreadyClosedException(UUID id) {
		super("Stay already closed: " + id);
	}
}
