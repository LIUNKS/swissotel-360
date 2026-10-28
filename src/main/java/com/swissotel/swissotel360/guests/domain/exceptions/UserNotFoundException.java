package com.swissotel.swissotel360.guests.domain.exceptions;

import java.util.UUID;

public class UserNotFoundException extends RuntimeException {
	public UserNotFoundException(UUID publicId) {
		super("IAM user not found: " + publicId);
	}
}
