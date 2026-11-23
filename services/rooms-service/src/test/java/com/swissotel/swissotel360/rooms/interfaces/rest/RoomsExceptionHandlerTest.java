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
import com.swissotel.swissotel360.rooms.domain.model.valueobjects.RoomStatus;
import com.swissotel.swissotel360.shared.interfaces.rest.resources.ErrorResource;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoomsExceptionHandlerTest {

	private final RoomsExceptionHandler handler = new RoomsExceptionHandler();

	@Test
	void mapsRoomsExceptionsWithoutInternalPackageLeaks() {
		var uuid = UUID.randomUUID();
		assertErrorResponse(handler.handleRoomTypeNotFound(new RoomTypeNotFoundException(uuid)),
				404, "ROOM_TYPE_NOT_FOUND", "Room type not found", uuid.toString());
		assertErrorResponse(handler.handleRoomNotFound(new RoomNotFoundException(uuid)),
				404, "ROOM_NOT_FOUND", "Room not found", uuid.toString());
		assertErrorResponse(handler.handleRateEntityNotFound(new RateEntityNotFoundException(uuid)),
				404, "RATE_NOT_FOUND", "Rate not found", uuid.toString());

		assertErrorResponse(handler.handleDuplicateRoomType(new DuplicateRoomTypeException("Suite")),
				409, "DUPLICATE_ROOM_TYPE", "Room type already exists", "Suite");
		assertErrorResponse(handler.handleDuplicateRoomNumber(new DuplicateRoomNumberException("401")),
				409, "DUPLICATE_ROOM_NUMBER", "Room number already exists", "401");
		assertErrorResponse(
				handler.handleRateOverlap(
						new RateOverlapException(List.of(LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 11)))),
				409, "RATE_OVERLAP", "Rate overlaps an existing period", "2026-11-10, 2026-11-11");
		assertErrorResponse(
				handler.handleInvalidRoomStatus(
						new InvalidRoomStatusTransitionException(RoomStatus.AVAILABLE, RoomStatus.OUT_OF_SERVICE)),
				409, "INVALID_STATUS_TRANSITION", "Invalid room status transition", "AVAILABLE to OUT_OF_SERVICE");
		assertErrorResponse(handler.handleRoomTypeInUse(new RoomTypeInUseException(uuid)),
				409, "ROOM_TYPE_IN_USE", "Room type is in use", uuid.toString());
		assertErrorResponse(handler.handleRoomInUse(new RoomInUseException(uuid)),
				409, "ROOM_IN_USE", "Room is in use", uuid.toString());
		assertErrorResponse(handler.handleOccupiedRoomTypeChange(new OccupiedRoomTypeChangeException(uuid)),
				409, "OCCUPIED_ROOM_TYPE_CHANGE", "Room type cannot change while occupied", uuid.toString());
		assertErrorResponse(handler.handleRateCannotBeDeleted(new RateCannotBeDeletedException(uuid)),
				409, "RATE_CANNOT_BE_DELETED", "Rate cannot be deleted", uuid.toString());

		assertErrorResponse(handler.handleRateNotFound(new RateNotFoundException(LocalDate.of(2026, 12, 24))),
				422, "RATE_NOT_FOUND", "Rate not found", "2026-12-24");
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
