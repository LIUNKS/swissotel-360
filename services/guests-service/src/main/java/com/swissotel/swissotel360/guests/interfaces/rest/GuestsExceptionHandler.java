package com.swissotel.swissotel360.guests.interfaces.rest;

import com.swissotel.swissotel360.guests.domain.exceptions.DocumentTypeNotFoundException;
import com.swissotel.swissotel360.guests.domain.exceptions.DuplicateGuestException;
import com.swissotel.swissotel360.guests.domain.exceptions.GuestNotFoundException;
import com.swissotel.swissotel360.guests.domain.exceptions.InvalidDocumentNumberException;
import com.swissotel.swissotel360.guests.domain.exceptions.UserAlreadyLinkedException;
import com.swissotel.swissotel360.guests.domain.exceptions.UserNotFoundException;
import com.swissotel.swissotel360.shared.interfaces.rest.resources.ErrorResource;
import com.swissotel.swissotel360.shared.interfaces.rest.transform.ErrorResponseAssembler;
import org.jspecify.annotations.NullMarked;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
@NullMarked
public class GuestsExceptionHandler {

	@ExceptionHandler(GuestNotFoundException.class)
	public ResponseEntity<ErrorResource> handleGuestNotFound(GuestNotFoundException ex) {
		return ErrorResponseAssembler.buildErrorResponse(HttpStatus.NOT_FOUND, "GUEST_NOT_FOUND", "Guest not found", ex);
	}

	@ExceptionHandler(DocumentTypeNotFoundException.class)
	public ResponseEntity<ErrorResource> handleDocumentTypeNotFound(DocumentTypeNotFoundException ex) {
		return ErrorResponseAssembler.buildErrorResponse(HttpStatus.NOT_FOUND, "DOCUMENT_TYPE_NOT_FOUND",
				"Document type not found", ex);
	}

	@ExceptionHandler(DuplicateGuestException.class)
	public ResponseEntity<ErrorResource> handleDuplicateGuest(DuplicateGuestException ex) {
		var details = "Document type %s, document number %s".formatted(ex.getDocumentTypeCode(), ex.getDocumentNumber());
		return ErrorResponseAssembler.buildErrorResponse(HttpStatus.CONFLICT, "DUPLICATE_DOCUMENT",
				"A guest with this document already exists", details);
	}

	@ExceptionHandler(InvalidDocumentNumberException.class)
	public ResponseEntity<ErrorResource> handleInvalidDocumentNumber(InvalidDocumentNumberException ex) {
		return ErrorResponseAssembler.buildErrorResponse(HttpStatus.BAD_REQUEST, "INVALID_DOCUMENT_NUMBER",
				"Invalid document number", ex);
	}

	@ExceptionHandler(UserNotFoundException.class)
	public ResponseEntity<ErrorResource> handleUserNotFound(UserNotFoundException ex) {
		return ErrorResponseAssembler.buildErrorResponse(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "User not found", ex);
	}

	@ExceptionHandler(UserAlreadyLinkedException.class)
	public ResponseEntity<ErrorResource> handleUserAlreadyLinked(UserAlreadyLinkedException ex) {
		return ErrorResponseAssembler.buildErrorResponse(HttpStatus.CONFLICT, "USER_ALREADY_LINKED",
				"User already linked to a guest", ex);
	}
}
