package com.swissotel.swissotel360.operations;

import com.swissotel.swissotel360.servicerequests.domain.exceptions.InvalidServiceRequestTransitionException;
import com.swissotel.swissotel360.servicerequests.domain.exceptions.ServiceRequestNotFoundException;
import com.swissotel.swissotel360.servicerequests.domain.exceptions.ServiceRequestStayNotFoundException;
import com.swissotel.swissotel360.servicerequests.domain.exceptions.ServiceTypeNotFoundException;
import com.swissotel.swissotel360.servicerequests.domain.exceptions.StayNotActiveForRequestException;
import com.swissotel.swissotel360.servicerequests.domain.model.ServiceRequestStatus;
import com.swissotel.swissotel360.servicerequests.interfaces.rest.ServiceRequestsExceptionHandler;
import com.swissotel.swissotel360.shared.interfaces.rest.resources.ErrorResource;
import com.swissotel.swissotel360.stays.domain.exceptions.DigitalKeyNotFoundException;
import com.swissotel.swissotel360.stays.domain.exceptions.GuestDocumentMismatchException;
import com.swissotel.swissotel360.stays.domain.exceptions.ReservationNotEligibleForCheckInException;
import com.swissotel.swissotel360.stays.domain.exceptions.RoomNotAvailableException;
import com.swissotel.swissotel360.stays.domain.exceptions.StayAlreadyClosedException;
import com.swissotel.swissotel360.stays.domain.exceptions.StayNotFoundException;
import com.swissotel.swissotel360.stays.domain.exceptions.StayReservationNotFoundException;
import com.swissotel.swissotel360.stays.domain.model.KeyStatus;
import com.swissotel.swissotel360.stays.domain.model.StayStatus;
import com.swissotel.swissotel360.stays.interfaces.rest.StaysExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OperationsExceptionHandlersTest {

	private final StaysExceptionHandler staysHandler = new StaysExceptionHandler();
	private final ServiceRequestsExceptionHandler serviceRequestsHandler = new ServiceRequestsExceptionHandler();

	@Test
	void mapsStaysAndServiceRequestsExceptionsWithoutInternalPackageLeaks() {
		var uuid = UUID.randomUUID();

		assertErrorResponse(staysHandler.handleStayNotFound(new StayNotFoundException(uuid)),
				404, "STAY_NOT_FOUND", "Stay not found", uuid.toString());
		assertErrorResponse(staysHandler.handleStayReservationNotFound(new StayReservationNotFoundException("SW26-XYZ")),
				404, "RESERVATION_NOT_FOUND", "Reservation not found", "SW26-XYZ");
		assertErrorResponse(staysHandler.handleDigitalKeyNotFound(new DigitalKeyNotFoundException(uuid)),
				404, "DIGITAL_KEY_NOT_FOUND", "Digital key not found", uuid.toString());
		assertErrorResponse(
				staysHandler.handleReservationNotEligibleForCheckIn(
						new ReservationNotEligibleForCheckInException("Reservation is cancelled")),
				409, "RESERVATION_NOT_ELIGIBLE_FOR_CHECK_IN",
				"Reservation not eligible for check-in", "Reservation is cancelled");
		assertErrorResponse(staysHandler.handleRoomNotAvailable(new RoomNotAvailableException("Room is cleaning")),
				409, "ROOM_NOT_AVAILABLE", "Room not available", "Room is cleaning");
		assertErrorResponse(staysHandler.handleStayAlreadyClosed(new StayAlreadyClosedException(uuid)),
				409, "STAY_ALREADY_CLOSED", "Stay is already closed", uuid.toString());
		assertErrorResponse(staysHandler.handleGuestDocumentMismatch(new GuestDocumentMismatchException()),
				422, "GUEST_DOCUMENT_MISMATCH", "Guest document does not match reservation", "does not match");

		assertErrorResponse(serviceRequestsHandler.handleServiceRequestNotFound(new ServiceRequestNotFoundException(uuid)),
				404, "SERVICE_REQUEST_NOT_FOUND", "Service request not found", uuid.toString());
		assertErrorResponse(serviceRequestsHandler.handleServiceTypeNotFound(new ServiceTypeNotFoundException("SPA")),
				404, "SERVICE_TYPE_NOT_FOUND", "Service type not found", "SPA");
		assertErrorResponse(serviceRequestsHandler.handleServiceRequestStayNotFound(new ServiceRequestStayNotFoundException(uuid)),
				404, "STAY_NOT_FOUND", "Stay not found", uuid.toString());
		assertErrorResponse(serviceRequestsHandler.handleStayNotActiveForRequest(new StayNotActiveForRequestException(uuid)),
				409, "STAY_NOT_ACTIVE", "Stay is not active", uuid.toString());
		assertErrorResponse(
				serviceRequestsHandler.handleInvalidServiceRequestTransition(
						new InvalidServiceRequestTransitionException("completed", "in_progress")),
				409, "INVALID_SERVICE_REQUEST_TRANSITION",
				"Invalid service request transition", "completed -> in_progress");
	}

	@Test
	void domainEnumsRejectInvalidCodesWithoutExposingJavaClassNames() {
		assertInvalidEnumCode(() -> ServiceRequestStatus.fromCode("zzz"), "Invalid service request status");
		assertInvalidEnumCode(() -> ServiceRequestStatus.fromCode(null), "Invalid service request status");
		assertInvalidEnumCode(() -> ServiceRequestStatus.fromCode("   "), "Invalid service request status");
		assertEquals(ServiceRequestStatus.IN_PROGRESS, ServiceRequestStatus.fromCode("in_progress"));

		assertInvalidEnumCode(() -> KeyStatus.fromCode("zzz"), "Invalid key status");
		assertInvalidEnumCode(() -> KeyStatus.fromCode(null), "Invalid key status");
		assertInvalidEnumCode(() -> KeyStatus.fromCode("   "), "Invalid key status");
		assertEquals(KeyStatus.ACTIVE, KeyStatus.fromCode("active"));

		assertInvalidEnumCode(() -> StayStatus.fromCode("zzz"), "Invalid stay status");
		assertInvalidEnumCode(() -> StayStatus.fromCode(null), "Invalid stay status");
		assertInvalidEnumCode(() -> StayStatus.fromCode("   "), "Invalid stay status");
		assertEquals(StayStatus.CLOSED, StayStatus.fromCode("closed"));
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

	private static void assertInvalidEnumCode(Runnable call, String expectedPrefix) {
		var ex = assertThrows(IllegalArgumentException.class, call::run);
		assertNotNull(ex.getMessage());
		assertTrue(ex.getMessage().startsWith(expectedPrefix));
		assertFalse(ex.getMessage().contains("com.swissotel"));
		assertFalse(ex.getMessage().contains("java."));
		assertFalse(ex.getMessage().contains("No enum constant"));
	}
}
