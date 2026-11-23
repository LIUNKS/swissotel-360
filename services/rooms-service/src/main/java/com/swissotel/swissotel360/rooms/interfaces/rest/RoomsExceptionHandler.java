package com.swissotel.swissotel360.rooms.interfaces.rest;

import com.swissotel.swissotel360.rooms.domain.exceptions.DuplicateRoomNumberException;
import com.swissotel.swissotel360.rooms.domain.exceptions.DuplicateRoomTypeException;
import com.swissotel.swissotel360.rooms.domain.exceptions.InvalidRoomStatusTransitionException;
import com.swissotel.swissotel360.rooms.domain.exceptions.OccupiedRoomTypeChangeException;
import com.swissotel.swissotel360.rooms.domain.exceptions.RateCannotBeDeletedException;
import com.swissotel.swissotel360.rooms.domain.exceptions.RateEntityNotFoundException;
import com.swissotel.swissotel360.rooms.domain.exceptions.RateNotFoundException;
import com.swissotel.swissotel360.rooms.domain.exceptions.RateOverlapException;
import com.swissotel.swissotel360.rooms.domain.exceptions.RoomInUseException;
import com.swissotel.swissotel360.rooms.domain.exceptions.RoomNotFoundException;
import com.swissotel.swissotel360.rooms.domain.exceptions.RoomTypeInUseException;
import com.swissotel.swissotel360.rooms.domain.exceptions.RoomTypeNotFoundException;
import com.swissotel.swissotel360.shared.interfaces.rest.resources.ErrorResource;
import com.swissotel.swissotel360.shared.interfaces.rest.transform.ErrorResponseAssembler;
import org.jspecify.annotations.NullMarked;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDate;
import java.util.stream.Collectors;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
@NullMarked
public class RoomsExceptionHandler {

	@ExceptionHandler(RoomTypeNotFoundException.class)
	public ResponseEntity<ErrorResource> handleRoomTypeNotFound(RoomTypeNotFoundException ex) {
		return ErrorResponseAssembler.buildErrorResponse(HttpStatus.NOT_FOUND, "ROOM_TYPE_NOT_FOUND",
				"Room type not found", ex);
	}

	@ExceptionHandler(RoomNotFoundException.class)
	public ResponseEntity<ErrorResource> handleRoomNotFound(RoomNotFoundException ex) {
		return ErrorResponseAssembler.buildErrorResponse(HttpStatus.NOT_FOUND, "ROOM_NOT_FOUND", "Room not found", ex);
	}

	@ExceptionHandler(RateEntityNotFoundException.class)
	public ResponseEntity<ErrorResource> handleRateEntityNotFound(RateEntityNotFoundException ex) {
		return ErrorResponseAssembler.buildErrorResponse(HttpStatus.NOT_FOUND, "RATE_NOT_FOUND", "Rate not found", ex);
	}

	@ExceptionHandler(DuplicateRoomTypeException.class)
	public ResponseEntity<ErrorResource> handleDuplicateRoomType(DuplicateRoomTypeException ex) {
		return ErrorResponseAssembler.buildErrorResponse(HttpStatus.CONFLICT, "DUPLICATE_ROOM_TYPE",
				"Room type already exists", ex);
	}

	@ExceptionHandler(DuplicateRoomNumberException.class)
	public ResponseEntity<ErrorResource> handleDuplicateRoomNumber(DuplicateRoomNumberException ex) {
		return ErrorResponseAssembler.buildErrorResponse(HttpStatus.CONFLICT, "DUPLICATE_ROOM_NUMBER",
				"Room number already exists", ex);
	}

	@ExceptionHandler(RateOverlapException.class)
	public ResponseEntity<ErrorResource> handleRateOverlap(RateOverlapException ex) {
		var conflicts = ex.getConflicts();
		var details = conflicts.isEmpty()
				? ErrorResponseAssembler.safeMessage(ex)
				: "Conflicting dates: " + conflicts.stream()
						.map(LocalDate::toString)
						.collect(Collectors.joining(", "));
		return ErrorResponseAssembler.buildErrorResponse(HttpStatus.CONFLICT, "RATE_OVERLAP",
				"Rate overlaps an existing period", details);
	}

	@ExceptionHandler(InvalidRoomStatusTransitionException.class)
	public ResponseEntity<ErrorResource> handleInvalidRoomStatus(InvalidRoomStatusTransitionException ex) {
		var details = "Cannot transition from %s to %s".formatted(ex.getFrom().name(), ex.getTo().name());
		return ErrorResponseAssembler.buildErrorResponse(HttpStatus.CONFLICT, "INVALID_STATUS_TRANSITION",
				"Invalid room status transition", details);
	}

	@ExceptionHandler(RoomTypeInUseException.class)
	public ResponseEntity<ErrorResource> handleRoomTypeInUse(RoomTypeInUseException ex) {
		return ErrorResponseAssembler.buildErrorResponse(HttpStatus.CONFLICT, "ROOM_TYPE_IN_USE",
				"Room type is in use", ex);
	}

	@ExceptionHandler(RoomInUseException.class)
	public ResponseEntity<ErrorResource> handleRoomInUse(RoomInUseException ex) {
		return ErrorResponseAssembler.buildErrorResponse(HttpStatus.CONFLICT, "ROOM_IN_USE", "Room is in use", ex);
	}

	@ExceptionHandler(OccupiedRoomTypeChangeException.class)
	public ResponseEntity<ErrorResource> handleOccupiedRoomTypeChange(OccupiedRoomTypeChangeException ex) {
		return ErrorResponseAssembler.buildErrorResponse(HttpStatus.CONFLICT, "OCCUPIED_ROOM_TYPE_CHANGE",
				"Room type cannot change while occupied", ex);
	}

	@ExceptionHandler(RateCannotBeDeletedException.class)
	public ResponseEntity<ErrorResource> handleRateCannotBeDeleted(RateCannotBeDeletedException ex) {
		return ErrorResponseAssembler.buildErrorResponse(HttpStatus.CONFLICT, "RATE_CANNOT_BE_DELETED",
				"Rate cannot be deleted", ex);
	}

	@ExceptionHandler(RateNotFoundException.class)
	public ResponseEntity<ErrorResource> handleRateNotFound(RateNotFoundException ex) {
		return ErrorResponseAssembler.buildErrorResponse(HttpStatus.UNPROCESSABLE_CONTENT, "RATE_NOT_FOUND",
				"Rate not found", "No effective rate for date: " + ex.getDate());
	}
}
