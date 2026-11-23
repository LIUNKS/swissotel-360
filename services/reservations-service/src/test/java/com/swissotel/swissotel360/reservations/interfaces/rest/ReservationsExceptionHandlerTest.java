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
import com.swissotel.swissotel360.reservations.domain.model.ReservationId;
import com.swissotel.swissotel360.reservations.domain.model.ReservationStatus;
import com.swissotel.swissotel360.reservations.domain.model.RoomTypeId;
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

class ReservationsExceptionHandlerTest {

	private final ReservationsExceptionHandler handler = new ReservationsExceptionHandler();

	@Test
	void mapsReservationsExceptionsWithoutInternalPackageLeaks() {
		var uuid = UUID.randomUUID();
		var reservationId = ReservationId.newId();

		assertErrorResponse(handler.handleReservationGuestNotFound(new GuestNotFoundException(uuid)),
				404, "GUEST_NOT_FOUND", "Guest not found", uuid.toString());
		assertErrorResponse(handler.handleReservationRoomTypeNotFound(new RoomTypeNotFoundException(uuid)),
				404, "ROOM_TYPE_NOT_FOUND", "Room type not found", uuid.toString());
		assertErrorResponse(handler.handleReservationNotFound(new ReservationNotFoundException("SW26-ABC")),
				404, "RESERVATION_NOT_FOUND", "Reservation not found", "SW26-ABC");

		assertErrorResponse(handler.handleNoAvailability(new NoAvailabilityException(new RoomTypeId(3L))),
				409, "NO_AVAILABILITY", "No room availability", "3");
		assertErrorResponse(handler.handleDuplicateReservation(new DuplicateReservationException()),
				409, "DUPLICATE_RESERVATION", "Duplicate reservation", "active reservation");
		assertErrorResponse(
				handler.handleInvalidReservationTransition(new InvalidReservationTransitionException(
						reservationId, ReservationStatus.PENDING, ReservationStatus.COMPLETED)),
				409, "INVALID_STATUS_TRANSITION", "Invalid reservation status transition", "pending to completed");
		assertErrorResponse(
				handler.handleReservationNotModifiable(
						new ReservationNotModifiableException(reservationId, ReservationStatus.CANCELLED)),
				409, "RESERVATION_NOT_MODIFIABLE", "Reservation is not modifiable", reservationId.value().toString());
		assertErrorResponse(handler.handleGuestProfileRequired(new GuestProfileRequiredException()),
				409, "GUEST_PROFILE_REQUIRED", "Guest profile required", "guest profile");

		assertErrorResponse(handler.handleReservationRateNotFound(new RateNotFoundException(LocalDate.of(2026, 12, 25))),
				422, "RATE_NOT_FOUND", "Rate not found", "2026-12-25");
		assertErrorResponse(handler.handleInvalidStayPeriod(new InvalidStayPeriodException("Check-out must be after check-in")),
				400, "INVALID_STAY_PERIOD", "Invalid stay period", "Check-out must be after check-in");
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
