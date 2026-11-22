package com.swissotel.swissotel360;

import com.swissotel.swissotel360.billing.domain.exceptions.*;
import com.swissotel.swissotel360.billing.domain.model.DocumentType;
import com.swissotel.swissotel360.billing.domain.model.FolioStatus;
import com.swissotel.swissotel360.billing.domain.model.PaymentStatus;
import com.swissotel.swissotel360.billing.interfaces.rest.BillingExceptionHandler;
import com.swissotel.swissotel360.guests.domain.exceptions.DocumentTypeNotFoundException;
import com.swissotel.swissotel360.guests.domain.exceptions.DuplicateGuestException;
import com.swissotel.swissotel360.guests.domain.exceptions.GuestNotFoundException;
import com.swissotel.swissotel360.guests.domain.exceptions.InvalidDocumentNumberException;
import com.swissotel.swissotel360.guests.domain.exceptions.UserAlreadyLinkedException;
import com.swissotel.swissotel360.guests.domain.exceptions.UserNotFoundException;
import com.swissotel.swissotel360.guests.interfaces.rest.GuestsExceptionHandler;
import com.swissotel.swissotel360.reservations.domain.exceptions.DuplicateReservationException;
import com.swissotel.swissotel360.reservations.domain.exceptions.GuestProfileRequiredException;
import com.swissotel.swissotel360.reservations.domain.exceptions.InvalidReservationTransitionException;
import com.swissotel.swissotel360.reservations.domain.exceptions.InvalidStayPeriodException;
import com.swissotel.swissotel360.reservations.domain.exceptions.NoAvailabilityException;
import com.swissotel.swissotel360.reservations.domain.exceptions.ReservationNotFoundException;
import com.swissotel.swissotel360.reservations.domain.exceptions.ReservationNotModifiableException;
import com.swissotel.swissotel360.reservations.domain.model.ReservationId;
import com.swissotel.swissotel360.reservations.domain.model.ReservationStatus;
import com.swissotel.swissotel360.reservations.domain.model.RoomTypeId;
import com.swissotel.swissotel360.reservations.interfaces.rest.ReservationsExceptionHandler;
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
import com.swissotel.swissotel360.rooms.interfaces.rest.RoomsExceptionHandler;
import com.swissotel.swissotel360.servicerequests.domain.exceptions.*;
import com.swissotel.swissotel360.servicerequests.domain.model.ServiceRequestStatus;
import com.swissotel.swissotel360.servicerequests.interfaces.rest.ServiceRequestsExceptionHandler;
import com.swissotel.swissotel360.shared.interfaces.rest.SecurityExceptionHandler;
import com.swissotel.swissotel360.shared.interfaces.rest.resources.ErrorResource;
import com.swissotel.swissotel360.stays.domain.exceptions.*;
import com.swissotel.swissotel360.stays.domain.model.KeyStatus;
import com.swissotel.swissotel360.stays.domain.model.StayStatus;
import com.swissotel.swissotel360.stays.interfaces.rest.StaysExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DomainExceptionHandlersTest {

	private final SecurityExceptionHandler securityHandler = new SecurityExceptionHandler();
	private final GuestsExceptionHandler guestsHandler = new GuestsExceptionHandler();
	private final RoomsExceptionHandler roomsHandler = new RoomsExceptionHandler();
	private final ReservationsExceptionHandler reservationsHandler = new ReservationsExceptionHandler();
	private final StaysExceptionHandler staysHandler = new StaysExceptionHandler();
	private final ServiceRequestsExceptionHandler serviceRequestsHandler = new ServiceRequestsExceptionHandler();
	private final BillingExceptionHandler billingHandler = new BillingExceptionHandler();

	@Test
	void mapsNotFoundExceptionsTo404ErrorResource() {
		var uuid = UUID.randomUUID();

		assertErrorResponse(guestsHandler.handleGuestNotFound(new GuestNotFoundException("guest-123")),
				404, "GUEST_NOT_FOUND", "Guest not found", "guest-123");
		assertErrorResponse(guestsHandler.handleDocumentTypeNotFound(new DocumentTypeNotFoundException("UNKNOWN")),
				404, "DOCUMENT_TYPE_NOT_FOUND", "Document type not found", "UNKNOWN");
		assertErrorResponse(guestsHandler.handleUserNotFound(new UserNotFoundException(uuid)),
				404, "USER_NOT_FOUND", "User not found", uuid.toString());
		assertErrorResponse(roomsHandler.handleRoomTypeNotFound(new RoomTypeNotFoundException(uuid)),
				404, "ROOM_TYPE_NOT_FOUND", "Room type not found", uuid.toString());
		assertErrorResponse(roomsHandler.handleRoomNotFound(new RoomNotFoundException(uuid)),
				404, "ROOM_NOT_FOUND", "Room not found", uuid.toString());
		assertErrorResponse(roomsHandler.handleRateEntityNotFound(new RateEntityNotFoundException(uuid)),
				404, "RATE_NOT_FOUND", "Rate not found", uuid.toString());
		assertErrorResponse(
				reservationsHandler.handleReservationGuestNotFound(
						new com.swissotel.swissotel360.reservations.domain.exceptions.GuestNotFoundException(uuid)),
				404, "GUEST_NOT_FOUND", "Guest not found", uuid.toString());
		assertErrorResponse(
				reservationsHandler.handleReservationRoomTypeNotFound(
						new com.swissotel.swissotel360.reservations.domain.exceptions.RoomTypeNotFoundException(uuid)),
				404, "ROOM_TYPE_NOT_FOUND", "Room type not found", uuid.toString());
		assertErrorResponse(reservationsHandler.handleReservationNotFound(new ReservationNotFoundException("SW26-ABC")),
				404, "RESERVATION_NOT_FOUND", "Reservation not found", "SW26-ABC");
		assertErrorResponse(staysHandler.handleStayNotFound(new StayNotFoundException(uuid)),
				404, "STAY_NOT_FOUND", "Stay not found", uuid.toString());
		assertErrorResponse(staysHandler.handleStayReservationNotFound(new StayReservationNotFoundException("SW26-XYZ")),
				404, "RESERVATION_NOT_FOUND", "Reservation not found", "SW26-XYZ");
		assertErrorResponse(staysHandler.handleDigitalKeyNotFound(new DigitalKeyNotFoundException(uuid)),
				404, "DIGITAL_KEY_NOT_FOUND", "Digital key not found", uuid.toString());
		assertErrorResponse(serviceRequestsHandler.handleServiceRequestNotFound(new ServiceRequestNotFoundException(uuid)),
				404, "SERVICE_REQUEST_NOT_FOUND", "Service request not found", uuid.toString());
		assertErrorResponse(serviceRequestsHandler.handleServiceTypeNotFound(new ServiceTypeNotFoundException("SPA")),
				404, "SERVICE_TYPE_NOT_FOUND", "Service type not found", "SPA");
		assertErrorResponse(serviceRequestsHandler.handleServiceRequestStayNotFound(new ServiceRequestStayNotFoundException(uuid)),
				404, "STAY_NOT_FOUND", "Stay not found", uuid.toString());
		assertErrorResponse(billingHandler.handleFolioNotFound(new FolioNotFoundException(uuid)),
				404, "FOLIO_NOT_FOUND", "Folio not found", uuid.toString());
		assertErrorResponse(billingHandler.handlePaymentMethodNotFound(new PaymentMethodNotFoundException("CARD")),
				404, "PAYMENT_METHOD_NOT_FOUND", "Payment method not found", "CARD");
		assertErrorResponse(billingHandler.handlePaymentNotFound(new PaymentNotFoundException(uuid)),
				404, "PAYMENT_NOT_FOUND", "Payment not found", uuid.toString());
		assertErrorResponse(billingHandler.handleChargeServiceRequestNotFound(new ChargeServiceRequestNotFoundException(uuid)),
				404, "SERVICE_REQUEST_NOT_FOUND", "Service request not found", uuid.toString());
		assertErrorResponse(billingHandler.handleBillingStayNotFound(new BillingStayNotFoundException(uuid)),
				404, "STAY_NOT_FOUND", "Stay not found", uuid.toString());
	}

	@Test
	void mapsConflictExceptionsTo409ErrorResource() {
		var uuid = UUID.randomUUID();
		var reservationId = ReservationId.newId();

		assertErrorResponse(guestsHandler.handleDuplicateGuest(new DuplicateGuestException("DNI", "12345678")),
				409, "DUPLICATE_DOCUMENT", "A guest with this document already exists", "12345678");
		assertErrorResponse(guestsHandler.handleUserAlreadyLinked(new UserAlreadyLinkedException()),
				409, "USER_ALREADY_LINKED", "User already linked to a guest", "already linked");
		assertErrorResponse(roomsHandler.handleDuplicateRoomType(new DuplicateRoomTypeException("Suite")),
				409, "DUPLICATE_ROOM_TYPE", "Room type already exists", "Suite");
		assertErrorResponse(roomsHandler.handleDuplicateRoomNumber(new DuplicateRoomNumberException("401")),
				409, "DUPLICATE_ROOM_NUMBER", "Room number already exists", "401");
		assertErrorResponse(
				roomsHandler.handleRateOverlap(
						new RateOverlapException(List.of(LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 11)))),
				409, "RATE_OVERLAP", "Rate overlaps an existing period", "2026-11-10, 2026-11-11");
		assertErrorResponse(
				roomsHandler.handleInvalidRoomStatus(
						new InvalidRoomStatusTransitionException(RoomStatus.AVAILABLE, RoomStatus.OUT_OF_SERVICE)),
				409, "INVALID_STATUS_TRANSITION", "Invalid room status transition", "AVAILABLE to OUT_OF_SERVICE");
		assertErrorResponse(roomsHandler.handleRoomTypeInUse(new RoomTypeInUseException(uuid)),
				409, "ROOM_TYPE_IN_USE", "Room type is in use", uuid.toString());
		assertErrorResponse(roomsHandler.handleRoomInUse(new RoomInUseException(uuid)),
				409, "ROOM_IN_USE", "Room is in use", uuid.toString());
		assertErrorResponse(roomsHandler.handleOccupiedRoomTypeChange(new OccupiedRoomTypeChangeException(uuid)),
				409, "OCCUPIED_ROOM_TYPE_CHANGE", "Room type cannot change while occupied", uuid.toString());
		assertErrorResponse(roomsHandler.handleRateCannotBeDeleted(new RateCannotBeDeletedException(uuid)),
				409, "RATE_CANNOT_BE_DELETED", "Rate cannot be deleted", uuid.toString());
		assertErrorResponse(reservationsHandler.handleNoAvailability(new NoAvailabilityException(new RoomTypeId(3L))),
				409, "NO_AVAILABILITY", "No room availability", "3");
		assertErrorResponse(reservationsHandler.handleDuplicateReservation(new DuplicateReservationException()),
				409, "DUPLICATE_RESERVATION", "Duplicate reservation", "active reservation");
		assertErrorResponse(
				reservationsHandler.handleInvalidReservationTransition(new InvalidReservationTransitionException(
						reservationId, ReservationStatus.PENDING, ReservationStatus.COMPLETED)),
				409, "INVALID_STATUS_TRANSITION", "Invalid reservation status transition", "pending to completed");
		assertErrorResponse(
				reservationsHandler.handleReservationNotModifiable(
						new ReservationNotModifiableException(reservationId, ReservationStatus.CANCELLED)),
				409, "RESERVATION_NOT_MODIFIABLE", "Reservation is not modifiable", reservationId.value().toString());
		assertErrorResponse(reservationsHandler.handleGuestProfileRequired(new GuestProfileRequiredException()),
				409, "GUEST_PROFILE_REQUIRED", "Guest profile required", "guest profile");
		assertErrorResponse(
				staysHandler.handleReservationNotEligibleForCheckIn(
						new ReservationNotEligibleForCheckInException("Reservation is cancelled")),
				409, "RESERVATION_NOT_ELIGIBLE_FOR_CHECK_IN",
				"Reservation not eligible for check-in", "Reservation is cancelled");
		assertErrorResponse(staysHandler.handleRoomNotAvailable(new RoomNotAvailableException("Room is cleaning")),
				409, "ROOM_NOT_AVAILABLE", "Room not available", "Room is cleaning");
		assertErrorResponse(serviceRequestsHandler.handleStayNotActiveForRequest(new StayNotActiveForRequestException(uuid)),
				409, "STAY_NOT_ACTIVE", "Stay is not active", uuid.toString());
		assertErrorResponse(
				serviceRequestsHandler.handleInvalidServiceRequestTransition(
						new InvalidServiceRequestTransitionException("completed", "in_progress")),
				409, "INVALID_SERVICE_REQUEST_TRANSITION",
				"Invalid service request transition", "completed -> in_progress");
		assertErrorResponse(staysHandler.handleStayAlreadyClosed(new StayAlreadyClosedException(uuid)),
				409, "STAY_ALREADY_CLOSED", "Stay is already closed", uuid.toString());
		assertErrorResponse(billingHandler.handleFolioClosed(new FolioClosedException()),
				409, "FOLIO_CLOSED", "Folio is closed", "closed");
		assertErrorResponse(billingHandler.handleFolioAlreadyPaid(new FolioAlreadyPaidException()),
				409, "FOLIO_ALREADY_PAID", "Folio is already paid", "payment");
		assertErrorResponse(billingHandler.handlePaymentNotProcessed(new PaymentNotProcessedException()),
				409, "PAYMENT_NOT_PROCESSED", "Payment is not processed", "not processed");
		assertErrorResponse(billingHandler.handleInvoiceAlreadyIssued(new InvoiceAlreadyIssuedException()),
				409, "INVOICE_ALREADY_ISSUED", "Invoice already issued", "already issued");
	}

	@Test
	void mapsUnprocessableContentExceptionsTo422ErrorResource() {
		assertErrorResponse(roomsHandler.handleRateNotFound(new RateNotFoundException(LocalDate.of(2026, 12, 24))),
				422, "RATE_NOT_FOUND", "Rate not found", "2026-12-24");
		assertErrorResponse(
				reservationsHandler.handleReservationRateNotFound(
						new com.swissotel.swissotel360.reservations.domain.exceptions.RateNotFoundException(
								LocalDate.of(2026, 12, 25))),
				422, "RATE_NOT_FOUND", "Rate not found", "2026-12-25");
		assertErrorResponse(staysHandler.handleGuestDocumentMismatch(new GuestDocumentMismatchException()),
				422, "GUEST_DOCUMENT_MISMATCH", "Guest document does not match reservation", "does not match");
		assertErrorResponse(billingHandler.handlePaymentAmountMismatch(new PaymentAmountMismatchException()),
				422, "PAYMENT_AMOUNT_MISMATCH", "Payment amount mismatch", "folio total");
	}

	@Test
	void mapsBadRequestAndForbiddenExceptionsToErrorResourceWithoutInternalLeaks() {
		assertErrorResponse(
				guestsHandler.handleInvalidDocumentNumber(new InvalidDocumentNumberException("Document must have 8 digits")),
				400, "INVALID_DOCUMENT_NUMBER", "Invalid document number", "Document must have 8 digits");
		assertErrorResponse(
				reservationsHandler.handleInvalidStayPeriod(new InvalidStayPeriodException("Check-out must be after check-in")),
				400, "INVALID_STAY_PERIOD", "Invalid stay period", "Check-out must be after check-in");

		var forbidden = securityHandler.handleAccessDenied(
				new AccessDeniedException("org.springframework.security.authorization.Denied: internal policy"));
		assertEquals(HttpStatus.FORBIDDEN, forbidden.getStatusCode());
		assertNotNull(forbidden.getBody());
		assertEquals("FORBIDDEN", forbidden.getBody().code());
		assertEquals("Permission denied", forbidden.getBody().message());
		assertEquals("Access denied", forbidden.getBody().details());
		assertNoInternalLeak(forbidden.getBody());
		assertFalse(forbidden.getBody().details().contains("internal policy"));
	}

	@Test
	void domainEnumsRejectInvalidCodesWithoutExposingJavaClassNames() {
		assertInvalidEnumCode(() -> DocumentType.fromCode("zzz"), "Invalid document type");
		assertInvalidEnumCode(() -> DocumentType.fromCode(null), "Invalid document type");
		assertInvalidEnumCode(() -> DocumentType.fromCode("   "), "Invalid document type");
		assertEquals(DocumentType.RECEIPT, DocumentType.fromCode("receipt"));

		assertInvalidEnumCode(() -> FolioStatus.fromCode("zzz"), "Invalid folio status");
		assertInvalidEnumCode(() -> FolioStatus.fromCode(null), "Invalid folio status");
		assertInvalidEnumCode(() -> FolioStatus.fromCode("   "), "Invalid folio status");
		assertEquals(FolioStatus.OPEN, FolioStatus.fromCode("open"));

		assertInvalidEnumCode(() -> PaymentStatus.fromCode("zzz"), "Invalid payment status");
		assertInvalidEnumCode(() -> PaymentStatus.fromCode(null), "Invalid payment status");
		assertInvalidEnumCode(() -> PaymentStatus.fromCode("   "), "Invalid payment status");
		assertEquals(PaymentStatus.PROCESSED, PaymentStatus.fromCode("processed"));

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
		assertTrue(body.details().contains(expectedDetailsSubstring),
				() -> "Expected details '%s' to contain '%s'".formatted(body.details(), expectedDetailsSubstring));
		assertNoInternalLeak(body);
	}

	private static void assertNoInternalLeak(ErrorResource resource) {
		for (var value : List.of(resource.code(), resource.message(),
				resource.details() == null ? "" : resource.details())) {
			assertFalse(value.contains("com.swissotel"), "Leaked package com.swissotel in: " + value);
			assertFalse(value.contains("java."), "Leaked package java. in: " + value);
			assertFalse(value.contains("org."), "Leaked package org. in: " + value);
		}
	}

	private static void assertInvalidEnumCode(Runnable call, String expectedPrefix) {
		var ex = assertThrows(IllegalArgumentException.class, call::run);
		assertNotNull(ex.getMessage());
		assertTrue(ex.getMessage().startsWith(expectedPrefix),
				() -> "Expected message '%s' to start with '%s'".formatted(ex.getMessage(), expectedPrefix));
		assertFalse(ex.getMessage().contains("com.swissotel"), "Leaked package in: " + ex.getMessage());
		assertFalse(ex.getMessage().contains("java."), "Leaked package in: " + ex.getMessage());
		assertFalse(ex.getMessage().contains("No enum constant"), "Leaked Enum.valueOf text in: " + ex.getMessage());
	}
}
