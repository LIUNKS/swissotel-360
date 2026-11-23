package com.swissotel.swissotel360.guests.interfaces.rest;

import com.swissotel.swissotel360.guests.domain.exceptions.DocumentTypeNotFoundException;
import com.swissotel.swissotel360.guests.domain.exceptions.DuplicateGuestException;
import com.swissotel.swissotel360.guests.domain.exceptions.GuestNotFoundException;
import com.swissotel.swissotel360.guests.domain.exceptions.InvalidDocumentNumberException;
import com.swissotel.swissotel360.guests.domain.exceptions.UserAlreadyLinkedException;
import com.swissotel.swissotel360.guests.domain.exceptions.UserNotFoundException;
import com.swissotel.swissotel360.shared.interfaces.rest.resources.ErrorResource;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GuestsExceptionHandlerTest {

	private final GuestsExceptionHandler handler = new GuestsExceptionHandler();

	@Test
	void mapsGuestExceptionsWithoutInternalPackageLeaks() {
		var uuid = UUID.randomUUID();
		assertErrorResponse(handler.handleGuestNotFound(new GuestNotFoundException("guest-123")),
				404, "GUEST_NOT_FOUND", "Guest not found", "guest-123");
		assertErrorResponse(handler.handleDocumentTypeNotFound(new DocumentTypeNotFoundException("UNKNOWN")),
				404, "DOCUMENT_TYPE_NOT_FOUND", "Document type not found", "UNKNOWN");
		assertErrorResponse(handler.handleUserNotFound(new UserNotFoundException(uuid)),
				404, "USER_NOT_FOUND", "User not found", uuid.toString());
		assertErrorResponse(handler.handleDuplicateGuest(new DuplicateGuestException("DNI", "12345678")),
				409, "DUPLICATE_DOCUMENT", "A guest with this document already exists", "12345678");
		assertErrorResponse(handler.handleUserAlreadyLinked(new UserAlreadyLinkedException()),
				409, "USER_ALREADY_LINKED", "User already linked to a guest", "already linked");
		assertErrorResponse(handler.handleInvalidDocumentNumber(new InvalidDocumentNumberException("Document must have 8 digits")),
				400, "INVALID_DOCUMENT_NUMBER", "Invalid document number", "Document must have 8 digits");
	}

	private static void assertErrorResponse(
			ResponseEntity<ErrorResource> response,
			int expectedStatus,
			String expectedCode,
			String expectedMessage,
			String expectedDetailsSubstring) {
		assertEquals(expectedStatus, response.getStatusCode().value());
		var body = response.getBody();
		assertNotNull(body);
		assertEquals(expectedCode, body.code());
		assertEquals(expectedMessage, body.message());
		assertNotNull(body.details());
		assertTrue(body.details().contains(expectedDetailsSubstring));
		for (var value : List.of(body.code(), body.message(), body.details())) {
			assertFalse(value.contains("com.swissotel"));
			assertFalse(value.contains("java."));
			assertFalse(value.contains("org."));
		}
	}
}
