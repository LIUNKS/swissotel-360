package com.swissotel.swissotel360.reservations.interfaces.rest;

import com.swissotel.swissotel360.reservations.domain.exceptions.DuplicateReservationException;
import com.swissotel.swissotel360.reservations.domain.exceptions.GuestNotFoundException;
import com.swissotel.swissotel360.reservations.domain.exceptions.GuestProfileRequiredException;
import com.swissotel.swissotel360.reservations.domain.exceptions.InvalidReservationTransitionException;
import com.swissotel.swissotel360.reservations.domain.exceptions.InvalidStayPeriodException;
import com.swissotel.swissotel360.reservations.domain.exceptions.NoAvailabilityException;
import com.swissotel.swissotel360.reservations.domain.exceptions.RateNotFoundException;
import com.swissotel.swissotel360.reservations.domain.exceptions.ReservationNotFoundException;
import com.swissotel.swissotel360.reservations.domain.exceptions.ReservationNotModifiableException;
import com.swissotel.swissotel360.reservations.domain.exceptions.RoomTypeNotFoundException;
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
public class ReservationsExceptionHandler {

	@ExceptionHandler(GuestNotFoundException.class)
	public ResponseEntity<ErrorResource> handleReservationGuestNotFound(GuestNotFoundException ex) {
		return ErrorResponseAssembler.buildErrorResponse(HttpStatus.NOT_FOUND, "GUEST_NOT_FOUND", "Guest not found", ex);
	}

	@ExceptionHandler(RoomTypeNotFoundException.class)
	public ResponseEntity<ErrorResource> handleReservationRoomTypeNotFound(RoomTypeNotFoundException ex) {
		return ErrorResponseAssembler.buildErrorResponse(HttpStatus.NOT_FOUND, "ROOM_TYPE_NOT_FOUND",
				"Room type not found", ex);
	}

	@ExceptionHandler(ReservationNotFoundException.class)
	public ResponseEntity<ErrorResource> handleReservationNotFound(ReservationNotFoundException ex) {
		return ErrorResponseAssembler.buildErrorResponse(HttpStatus.NOT_FOUND, "RESERVATION_NOT_FOUND",
				"Reservation not found", ex);
	}

	@ExceptionHandler(InvalidStayPeriodException.class)
	public ResponseEntity<ErrorResource> handleInvalidStayPeriod(InvalidStayPeriodException ex) {
		var reason = ex.getReason();
		var details = reason != null && !reason.isBlank() ? reason : ErrorResponseAssembler.safeMessage(ex);
		return ErrorResponseAssembler.buildErrorResponse(HttpStatus.BAD_REQUEST, "INVALID_STAY_PERIOD",
				"Invalid stay period", details);
	}

	@ExceptionHandler(NoAvailabilityException.class)
	public ResponseEntity<ErrorResource> handleNoAvailability(NoAvailabilityException ex) {
		return ErrorResponseAssembler.buildErrorResponse(HttpStatus.CONFLICT, "NO_AVAILABILITY",
				"No room availability", ex);
	}

	@ExceptionHandler(DuplicateReservationException.class)
	public ResponseEntity<ErrorResource> handleDuplicateReservation(DuplicateReservationException ex) {
		return ErrorResponseAssembler.buildErrorResponse(HttpStatus.CONFLICT, "DUPLICATE_RESERVATION",
				"Duplicate reservation", ex);
	}

	@ExceptionHandler(InvalidReservationTransitionException.class)
	public ResponseEntity<ErrorResource> handleInvalidReservationTransition(
			InvalidReservationTransitionException ex) {
		var details = "Cannot transition from %s to %s".formatted(ex.getFrom().code(), ex.getTo().code());
		return ErrorResponseAssembler.buildErrorResponse(HttpStatus.CONFLICT, "INVALID_STATUS_TRANSITION",
				"Invalid reservation status transition", details);
	}

	@ExceptionHandler(ReservationNotModifiableException.class)
	public ResponseEntity<ErrorResource> handleReservationNotModifiable(ReservationNotModifiableException ex) {
		return ErrorResponseAssembler.buildErrorResponse(HttpStatus.CONFLICT, "RESERVATION_NOT_MODIFIABLE",
				"Reservation is not modifiable", ex);
	}

	@ExceptionHandler(GuestProfileRequiredException.class)
	public ResponseEntity<ErrorResource> handleGuestProfileRequired(GuestProfileRequiredException ex) {
		return ErrorResponseAssembler.buildErrorResponse(HttpStatus.CONFLICT, "GUEST_PROFILE_REQUIRED",
				"Guest profile required", ex);
	}

	@ExceptionHandler(RateNotFoundException.class)
	public ResponseEntity<ErrorResource> handleReservationRateNotFound(RateNotFoundException ex) {
		return ErrorResponseAssembler.buildErrorResponse(HttpStatus.UNPROCESSABLE_CONTENT, "RATE_NOT_FOUND",
				"Rate not found", "No rate configured for date: " + ex.getDate());
	}
}
