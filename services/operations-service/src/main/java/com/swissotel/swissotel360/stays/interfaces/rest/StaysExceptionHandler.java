package com.swissotel.swissotel360.stays.interfaces.rest;

import com.swissotel.swissotel360.shared.interfaces.rest.resources.ErrorResource;
import com.swissotel.swissotel360.shared.interfaces.rest.transform.ErrorResponseAssembler;
import com.swissotel.swissotel360.stays.domain.exceptions.DigitalKeyNotFoundException;
import com.swissotel.swissotel360.stays.domain.exceptions.GuestDocumentMismatchException;
import com.swissotel.swissotel360.stays.domain.exceptions.ReservationNotEligibleForCheckInException;
import com.swissotel.swissotel360.stays.domain.exceptions.RoomNotAvailableException;
import com.swissotel.swissotel360.stays.domain.exceptions.StayAlreadyClosedException;
import com.swissotel.swissotel360.stays.domain.exceptions.StayNotFoundException;
import com.swissotel.swissotel360.stays.domain.exceptions.StayReservationNotFoundException;
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
public class StaysExceptionHandler {

	@ExceptionHandler(StayNotFoundException.class)
	public ResponseEntity<ErrorResource> handleStayNotFound(StayNotFoundException ex) {
		return ErrorResponseAssembler.buildErrorResponse(HttpStatus.NOT_FOUND, "STAY_NOT_FOUND", "Stay not found", ex);
	}

	@ExceptionHandler(StayReservationNotFoundException.class)
	public ResponseEntity<ErrorResource> handleStayReservationNotFound(StayReservationNotFoundException ex) {
		return ErrorResponseAssembler.buildErrorResponse(HttpStatus.NOT_FOUND, "RESERVATION_NOT_FOUND",
				"Reservation not found", ex);
	}

	@ExceptionHandler(DigitalKeyNotFoundException.class)
	public ResponseEntity<ErrorResource> handleDigitalKeyNotFound(DigitalKeyNotFoundException ex) {
		return ErrorResponseAssembler.buildErrorResponse(HttpStatus.NOT_FOUND, "DIGITAL_KEY_NOT_FOUND",
				"Digital key not found", ex);
	}

	@ExceptionHandler(ReservationNotEligibleForCheckInException.class)
	public ResponseEntity<ErrorResource> handleReservationNotEligibleForCheckIn(
			ReservationNotEligibleForCheckInException ex) {
		return ErrorResponseAssembler.buildErrorResponse(HttpStatus.CONFLICT, "RESERVATION_NOT_ELIGIBLE_FOR_CHECK_IN",
				"Reservation not eligible for check-in", ex);
	}

	@ExceptionHandler(RoomNotAvailableException.class)
	public ResponseEntity<ErrorResource> handleRoomNotAvailable(RoomNotAvailableException ex) {
		return ErrorResponseAssembler.buildErrorResponse(HttpStatus.CONFLICT, "ROOM_NOT_AVAILABLE",
				"Room not available", ex);
	}

	@ExceptionHandler(StayAlreadyClosedException.class)
	public ResponseEntity<ErrorResource> handleStayAlreadyClosed(StayAlreadyClosedException ex) {
		return ErrorResponseAssembler.buildErrorResponse(HttpStatus.CONFLICT, "STAY_ALREADY_CLOSED",
				"Stay is already closed", ex);
	}

	@ExceptionHandler(GuestDocumentMismatchException.class)
	public ResponseEntity<ErrorResource> handleGuestDocumentMismatch(GuestDocumentMismatchException ex) {
		return ErrorResponseAssembler.buildErrorResponse(HttpStatus.UNPROCESSABLE_CONTENT, "GUEST_DOCUMENT_MISMATCH",
				"Guest document does not match reservation", ex);
	}
}
