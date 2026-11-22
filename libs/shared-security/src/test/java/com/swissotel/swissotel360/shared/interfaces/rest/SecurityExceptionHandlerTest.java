package com.swissotel.swissotel360.shared.interfaces.rest;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SecurityExceptionHandlerTest {

	private final SecurityExceptionHandler handler = new SecurityExceptionHandler();

	@Test
	void handleAccessDeniedReturns403WithoutExposingInternalDetails() {
		var response = handler.handleAccessDenied(
				new AccessDeniedException("org.springframework.security.authorization.Denied: internal policy"));

		assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals("FORBIDDEN", response.getBody().code());
		assertEquals("Permission denied", response.getBody().message());
		assertEquals("Access denied", response.getBody().details());
		assertFalse(response.getBody().details().contains("internal policy"));
		assertFalse(response.getBody().details().contains("org."));
	}
}
