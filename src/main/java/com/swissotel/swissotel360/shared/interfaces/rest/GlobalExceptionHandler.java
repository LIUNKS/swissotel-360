package com.swissotel.swissotel360.shared.interfaces.rest;

import com.swissotel.swissotel360.shared.application.result.ApplicationError;
import com.swissotel.swissotel360.shared.interfaces.rest.transform.ErrorResponseAssembler;
import com.swissotel.swissotel360.guests.domain.exceptions.DocumentTypeNotFoundException;
import com.swissotel.swissotel360.guests.domain.exceptions.DuplicateGuestException;
import com.swissotel.swissotel360.guests.domain.exceptions.GuestNotFoundException;
import com.swissotel.swissotel360.guests.domain.exceptions.InvalidDocumentNumberException;
import com.swissotel.swissotel360.guests.domain.exceptions.UserAlreadyLinkedException;
import com.swissotel.swissotel360.guests.domain.exceptions.UserNotFoundException;
import com.swissotel.swissotel360.rooms.domain.exceptions.RoomTypeNotFoundException;
import com.swissotel.swissotel360.rooms.domain.exceptions.RoomNotFoundException;
import com.swissotel.swissotel360.rooms.domain.exceptions.DuplicateRoomTypeException;
import com.swissotel.swissotel360.rooms.domain.exceptions.DuplicateRoomNumberException;
import com.swissotel.swissotel360.rooms.domain.exceptions.RateOverlapException;
import com.swissotel.swissotel360.rooms.domain.exceptions.InvalidRoomStatusTransitionException;
import com.swissotel.swissotel360.rooms.domain.exceptions.RoomTypeInUseException;
import com.swissotel.swissotel360.rooms.domain.exceptions.RateNotFoundException;
import com.swissotel.swissotel360.rooms.domain.exceptions.RateEntityNotFoundException;
import com.swissotel.swissotel360.rooms.domain.exceptions.RateCannotBeDeletedException;
import com.swissotel.swissotel360.rooms.domain.exceptions.RoomInUseException;
import com.swissotel.swissotel360.rooms.domain.exceptions.OccupiedRoomTypeChangeException;
import com.swissotel.swissotel360.shared.interfaces.rest.resources.ErrorResource;
import com.swissotel.swissotel360.reservations.domain.exceptions.DuplicateReservationException;
import com.swissotel.swissotel360.reservations.domain.exceptions.GuestProfileRequiredException;
import com.swissotel.swissotel360.reservations.domain.exceptions.InvalidReservationTransitionException;
import com.swissotel.swissotel360.reservations.domain.exceptions.InvalidStayPeriodException;
import com.swissotel.swissotel360.reservations.domain.exceptions.NoAvailabilityException;
import com.swissotel.swissotel360.reservations.domain.exceptions.ReservationNotFoundException;
import com.swissotel.swissotel360.reservations.domain.exceptions.ReservationNotModifiableException;

import com.swissotel.swissotel360.billing.domain.exceptions.*;
import com.swissotel.swissotel360.servicerequests.domain.exceptions.*;
import com.swissotel.swissotel360.stays.domain.exceptions.*;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Global exception handler for REST API.
 * Provides centralized exception handling for the entire application,
 * ensuring all unhandled exceptions are translated to consistent
 * HTTP responses via the shared error assembly pattern.
 */
@RestControllerAdvice
@NullMarked
public class GlobalExceptionHandler {
	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
	private static final String MESSAGES_BASENAME = "messages";

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ErrorResource> handleAccessDenied(AccessDeniedException ex) {
		return buildErrorResponse(HttpStatus.FORBIDDEN, "FORBIDDEN", "Permission denied", "Access denied");
	}

	@ExceptionHandler(GuestNotFoundException.class)
	public ResponseEntity<ErrorResource> handleGuestNotFound(GuestNotFoundException ex) {
		return buildErrorResponse(HttpStatus.NOT_FOUND, "GUEST_NOT_FOUND", "Guest not found", ex);
	}

	@ExceptionHandler(DocumentTypeNotFoundException.class)
	public ResponseEntity<ErrorResource> handleDocumentTypeNotFound(DocumentTypeNotFoundException ex) {
		return buildErrorResponse(HttpStatus.NOT_FOUND, "DOCUMENT_TYPE_NOT_FOUND", "Document type not found", ex);
	}

	@ExceptionHandler(DuplicateGuestException.class)
	public ResponseEntity<ErrorResource> handleDuplicateGuest(DuplicateGuestException ex) {
		var details = "Document type %s, document number %s".formatted(ex.getDocumentTypeId(), ex.getDocumentNumber());
		return buildErrorResponse(HttpStatus.CONFLICT, "DUPLICATE_DOCUMENT",
				"A guest with this document already exists", details);
	}

	@ExceptionHandler(InvalidDocumentNumberException.class)
	public ResponseEntity<ErrorResource> handleInvalidDocumentNumber(InvalidDocumentNumberException ex) {
		return buildErrorResponse(HttpStatus.BAD_REQUEST, "INVALID_DOCUMENT_NUMBER", "Invalid document number", ex);
	}

	@ExceptionHandler(UserNotFoundException.class)
	public ResponseEntity<ErrorResource> handleUserNotFound(UserNotFoundException ex) {
		return buildErrorResponse(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "User not found", ex);
	}

	@ExceptionHandler(UserAlreadyLinkedException.class)
	public ResponseEntity<ErrorResource> handleUserAlreadyLinked(UserAlreadyLinkedException ex) {
		return buildErrorResponse(HttpStatus.CONFLICT, "USER_ALREADY_LINKED",
				"User already linked to a guest", ex);
	}

	@ExceptionHandler(RoomTypeNotFoundException.class)
	public ResponseEntity<ErrorResource> handleRoomTypeNotFound(RoomTypeNotFoundException ex) {
		return buildErrorResponse(HttpStatus.NOT_FOUND, "ROOM_TYPE_NOT_FOUND", "Room type not found", ex);
	}

	@ExceptionHandler(RoomNotFoundException.class)
	public ResponseEntity<ErrorResource> handleRoomNotFound(RoomNotFoundException ex) {
		return buildErrorResponse(HttpStatus.NOT_FOUND, "ROOM_NOT_FOUND", "Room not found", ex);
	}

	@ExceptionHandler(RateEntityNotFoundException.class)
	public ResponseEntity<ErrorResource> handleRateEntityNotFound(RateEntityNotFoundException ex) {
		return buildErrorResponse(HttpStatus.NOT_FOUND, "RATE_NOT_FOUND", "Rate not found", ex);
	}

	@ExceptionHandler(DuplicateRoomTypeException.class)
	public ResponseEntity<ErrorResource> handleDuplicateRoomType(DuplicateRoomTypeException ex) {
		return buildErrorResponse(HttpStatus.CONFLICT, "DUPLICATE_ROOM_TYPE", "Room type already exists", ex);
	}

	@ExceptionHandler(DuplicateRoomNumberException.class)
	public ResponseEntity<ErrorResource> handleDuplicateRoomNumber(DuplicateRoomNumberException ex) {
		return buildErrorResponse(HttpStatus.CONFLICT, "DUPLICATE_ROOM_NUMBER", "Room number already exists", ex);
	}

	@ExceptionHandler(RateOverlapException.class)
	public ResponseEntity<ErrorResource> handleRateOverlap(RateOverlapException ex) {
		var conflicts = ex.getConflicts();
		var details = conflicts.isEmpty()
				? safeMessage(ex)
				: "Conflicting dates: " + conflicts.stream()
						.map(LocalDate::toString)
						.collect(Collectors.joining(", "));
		return buildErrorResponse(HttpStatus.CONFLICT, "RATE_OVERLAP", "Rate overlaps an existing period", details);
	}

	@ExceptionHandler(InvalidRoomStatusTransitionException.class)
	public ResponseEntity<ErrorResource> handleInvalidRoomStatus(InvalidRoomStatusTransitionException ex) {
		var details = "Cannot transition from %s to %s".formatted(ex.getFrom().name(), ex.getTo().name());
		return buildErrorResponse(HttpStatus.CONFLICT, "INVALID_STATUS_TRANSITION",
				"Invalid room status transition", details);
	}

	@ExceptionHandler(RoomTypeInUseException.class)
	public ResponseEntity<ErrorResource> handleRoomTypeInUse(RoomTypeInUseException ex) {
		return buildErrorResponse(HttpStatus.CONFLICT, "ROOM_TYPE_IN_USE", "Room type is in use", ex);
	}

	@ExceptionHandler(RoomInUseException.class)
	public ResponseEntity<ErrorResource> handleRoomInUse(RoomInUseException ex) {
		return buildErrorResponse(HttpStatus.CONFLICT, "ROOM_IN_USE", "Room is in use", ex);
	}

	@ExceptionHandler(OccupiedRoomTypeChangeException.class)
	public ResponseEntity<ErrorResource> handleOccupiedRoomTypeChange(OccupiedRoomTypeChangeException ex) {
		return buildErrorResponse(HttpStatus.CONFLICT, "OCCUPIED_ROOM_TYPE_CHANGE",
				"Room type cannot change while occupied", ex);
	}

	@ExceptionHandler(RateCannotBeDeletedException.class)
	public ResponseEntity<ErrorResource> handleRateCannotBeDeleted(RateCannotBeDeletedException ex) {
		return buildErrorResponse(HttpStatus.CONFLICT, "RATE_CANNOT_BE_DELETED", "Rate cannot be deleted", ex);
	}

	@ExceptionHandler(RateNotFoundException.class)
	public ResponseEntity<ErrorResource> handleRateNotFound(RateNotFoundException ex) {
		return buildErrorResponse(HttpStatus.UNPROCESSABLE_CONTENT, "RATE_NOT_FOUND",
				"Rate not found", "No effective rate for date: " + ex.getDate());
	}

	@ExceptionHandler(com.swissotel.swissotel360.reservations.domain.exceptions.GuestNotFoundException.class)
	public ResponseEntity<ErrorResource> handleReservationGuestNotFound(
			com.swissotel.swissotel360.reservations.domain.exceptions.GuestNotFoundException ex) {
		return buildErrorResponse(HttpStatus.NOT_FOUND, "GUEST_NOT_FOUND", "Guest not found", ex);
	}

	@ExceptionHandler(com.swissotel.swissotel360.reservations.domain.exceptions.RoomTypeNotFoundException.class)
	public ResponseEntity<ErrorResource> handleReservationRoomTypeNotFound(
			com.swissotel.swissotel360.reservations.domain.exceptions.RoomTypeNotFoundException ex) {
		return buildErrorResponse(HttpStatus.NOT_FOUND, "ROOM_TYPE_NOT_FOUND", "Room type not found", ex);
	}

	@ExceptionHandler(ReservationNotFoundException.class)
	public ResponseEntity<ErrorResource> handleReservationNotFound(ReservationNotFoundException ex) {
		return buildErrorResponse(HttpStatus.NOT_FOUND, "RESERVATION_NOT_FOUND", "Reservation not found", ex);
	}

	@ExceptionHandler(InvalidStayPeriodException.class)
	public ResponseEntity<ErrorResource> handleInvalidStayPeriod(InvalidStayPeriodException ex) {
		var reason = ex.getReason();
		var details = reason != null && !reason.isBlank() ? reason : safeMessage(ex);
		return buildErrorResponse(HttpStatus.BAD_REQUEST, "INVALID_STAY_PERIOD", "Invalid stay period", details);
	}

	@ExceptionHandler(NoAvailabilityException.class)
	public ResponseEntity<ErrorResource> handleNoAvailability(NoAvailabilityException ex) {
		return buildErrorResponse(HttpStatus.CONFLICT, "NO_AVAILABILITY", "No room availability", ex);
	}

	@ExceptionHandler(DuplicateReservationException.class)
	public ResponseEntity<ErrorResource> handleDuplicateReservation(DuplicateReservationException ex) {
		return buildErrorResponse(HttpStatus.CONFLICT, "DUPLICATE_RESERVATION", "Duplicate reservation", ex);
	}

	@ExceptionHandler(InvalidReservationTransitionException.class)
	public ResponseEntity<ErrorResource> handleInvalidReservationTransition(
			InvalidReservationTransitionException ex) {
		var details = "Cannot transition from %s to %s".formatted(ex.getFrom().code(), ex.getTo().code());
		return buildErrorResponse(HttpStatus.CONFLICT, "INVALID_STATUS_TRANSITION",
				"Invalid reservation status transition", details);
	}

	@ExceptionHandler(ReservationNotModifiableException.class)
	public ResponseEntity<ErrorResource> handleReservationNotModifiable(ReservationNotModifiableException ex) {
		return buildErrorResponse(HttpStatus.CONFLICT, "RESERVATION_NOT_MODIFIABLE",
				"Reservation is not modifiable", ex);
	}

	@ExceptionHandler(GuestProfileRequiredException.class)
	public ResponseEntity<ErrorResource> handleGuestProfileRequired(GuestProfileRequiredException ex) {
		return buildErrorResponse(HttpStatus.CONFLICT, "GUEST_PROFILE_REQUIRED", "Guest profile required", ex);
	}

	@ExceptionHandler(com.swissotel.swissotel360.reservations.domain.exceptions.RateNotFoundException.class)
	public ResponseEntity<ErrorResource> handleReservationRateNotFound(
			com.swissotel.swissotel360.reservations.domain.exceptions.RateNotFoundException ex) {
		return buildErrorResponse(HttpStatus.UNPROCESSABLE_CONTENT, "RATE_NOT_FOUND",
				"Rate not found", "No rate configured for date: " + ex.getDate());
	}

	@ExceptionHandler(StayNotFoundException.class)
	public ResponseEntity<ErrorResource> handleStayNotFound(StayNotFoundException ex) {
		return buildErrorResponse(HttpStatus.NOT_FOUND, "STAY_NOT_FOUND", "Stay not found", ex);
	}

	@ExceptionHandler(StayReservationNotFoundException.class)
	public ResponseEntity<ErrorResource> handleStayReservationNotFound(StayReservationNotFoundException ex) {
		return buildErrorResponse(HttpStatus.NOT_FOUND, "RESERVATION_NOT_FOUND", "Reservation not found", ex);
	}

	@ExceptionHandler(DigitalKeyNotFoundException.class)
	public ResponseEntity<ErrorResource> handleDigitalKeyNotFound(DigitalKeyNotFoundException ex) {
		return buildErrorResponse(HttpStatus.NOT_FOUND, "DIGITAL_KEY_NOT_FOUND", "Digital key not found", ex);
	}

	@ExceptionHandler(ReservationNotEligibleForCheckInException.class)
	public ResponseEntity<ErrorResource> handleReservationNotEligibleForCheckIn(
			ReservationNotEligibleForCheckInException ex) {
		return buildErrorResponse(HttpStatus.CONFLICT, "RESERVATION_NOT_ELIGIBLE_FOR_CHECK_IN",
				"Reservation not eligible for check-in", ex);
	}

	@ExceptionHandler(RoomNotAvailableException.class)
	public ResponseEntity<ErrorResource> handleRoomNotAvailable(RoomNotAvailableException ex) {
		return buildErrorResponse(HttpStatus.CONFLICT, "ROOM_NOT_AVAILABLE", "Room not available", ex);
	}

	@ExceptionHandler(ServiceRequestNotFoundException.class)
	public ResponseEntity<ErrorResource> handleServiceRequestNotFound(ServiceRequestNotFoundException ex) {
		return buildErrorResponse(HttpStatus.NOT_FOUND, "SERVICE_REQUEST_NOT_FOUND",
				"Service request not found", ex);
	}

	@ExceptionHandler(ServiceTypeNotFoundException.class)
	public ResponseEntity<ErrorResource> handleServiceTypeNotFound(ServiceTypeNotFoundException ex) {
		return buildErrorResponse(HttpStatus.NOT_FOUND, "SERVICE_TYPE_NOT_FOUND", "Service type not found", ex);
	}

	@ExceptionHandler(ServiceRequestStayNotFoundException.class)
	public ResponseEntity<ErrorResource> handleServiceRequestStayNotFound(ServiceRequestStayNotFoundException ex) {
		return buildErrorResponse(HttpStatus.NOT_FOUND, "STAY_NOT_FOUND", "Stay not found", ex);
	}

	@ExceptionHandler(StayNotActiveForRequestException.class)
	public ResponseEntity<ErrorResource> handleStayNotActiveForRequest(StayNotActiveForRequestException ex) {
		return buildErrorResponse(HttpStatus.CONFLICT, "STAY_NOT_ACTIVE", "Stay is not active", ex);
	}

	@ExceptionHandler(InvalidServiceRequestTransitionException.class)
	public ResponseEntity<ErrorResource> handleInvalidServiceRequestTransition(
			InvalidServiceRequestTransitionException ex) {
		return buildErrorResponse(HttpStatus.CONFLICT, "INVALID_SERVICE_REQUEST_TRANSITION",
				"Invalid service request transition", ex);
	}

	@ExceptionHandler(StayAlreadyClosedException.class)
	public ResponseEntity<ErrorResource> handleStayAlreadyClosed(StayAlreadyClosedException ex) {
		return buildErrorResponse(HttpStatus.CONFLICT, "STAY_ALREADY_CLOSED", "Stay is already closed", ex);
	}

	@ExceptionHandler(GuestDocumentMismatchException.class)
	public ResponseEntity<ErrorResource> handleGuestDocumentMismatch(GuestDocumentMismatchException ex) {
		return buildErrorResponse(HttpStatus.UNPROCESSABLE_CONTENT, "GUEST_DOCUMENT_MISMATCH",
				"Guest document does not match reservation", ex);
	}

	@ExceptionHandler(FolioNotFoundException.class)
	public ResponseEntity<ErrorResource> handleFolioNotFound(FolioNotFoundException ex) {
		return buildErrorResponse(HttpStatus.NOT_FOUND, "FOLIO_NOT_FOUND", "Folio not found", ex);
	}

	@ExceptionHandler(FolioClosedException.class)
	public ResponseEntity<ErrorResource> handleFolioClosed(FolioClosedException ex) {
		return buildErrorResponse(HttpStatus.CONFLICT, "FOLIO_CLOSED", "Folio is closed", ex);
	}

	@ExceptionHandler(PaymentMethodNotFoundException.class)
	public ResponseEntity<ErrorResource> handlePaymentMethodNotFound(PaymentMethodNotFoundException ex) {
		return buildErrorResponse(HttpStatus.NOT_FOUND, "PAYMENT_METHOD_NOT_FOUND",
				"Payment method not found", ex);
	}

	@ExceptionHandler(PaymentAmountMismatchException.class)
	public ResponseEntity<ErrorResource> handlePaymentAmountMismatch(PaymentAmountMismatchException ex) {
		return buildErrorResponse(HttpStatus.UNPROCESSABLE_CONTENT, "PAYMENT_AMOUNT_MISMATCH",
				"Payment amount mismatch", ex);
	}

	@ExceptionHandler(FolioAlreadyPaidException.class)
	public ResponseEntity<ErrorResource> handleFolioAlreadyPaid(FolioAlreadyPaidException ex) {
		return buildErrorResponse(HttpStatus.CONFLICT, "FOLIO_ALREADY_PAID", "Folio is already paid", ex);
	}

	@ExceptionHandler(PaymentNotFoundException.class)
	public ResponseEntity<ErrorResource> handlePaymentNotFound(PaymentNotFoundException ex) {
		return buildErrorResponse(HttpStatus.NOT_FOUND, "PAYMENT_NOT_FOUND", "Payment not found", ex);
	}

	@ExceptionHandler(PaymentNotProcessedException.class)
	public ResponseEntity<ErrorResource> handlePaymentNotProcessed(PaymentNotProcessedException ex) {
		return buildErrorResponse(HttpStatus.CONFLICT, "PAYMENT_NOT_PROCESSED", "Payment is not processed", ex);
	}

	@ExceptionHandler(InvoiceAlreadyIssuedException.class)
	public ResponseEntity<ErrorResource> handleInvoiceAlreadyIssued(InvoiceAlreadyIssuedException ex) {
		return buildErrorResponse(HttpStatus.CONFLICT, "INVOICE_ALREADY_ISSUED", "Invoice already issued", ex);
	}

	@ExceptionHandler(ChargeServiceRequestNotFoundException.class)
	public ResponseEntity<ErrorResource> handleChargeServiceRequestNotFound(
			ChargeServiceRequestNotFoundException ex) {
		return buildErrorResponse(HttpStatus.NOT_FOUND, "SERVICE_REQUEST_NOT_FOUND",
				"Service request not found", ex);
	}

	@ExceptionHandler(BillingStayNotFoundException.class)
	public ResponseEntity<ErrorResource> handleBillingStayNotFound(BillingStayNotFoundException ex) {
		return buildErrorResponse(HttpStatus.NOT_FOUND, "STAY_NOT_FOUND", "Stay not found", ex);
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ErrorResource> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
		var parameterName = ex.getName();
		var safeParameterName = parameterName != null && !parameterName.isBlank() ? parameterName : "unknown";
		return buildErrorResponse(
				HttpStatus.BAD_REQUEST,
				"INVALID_REQUEST",
				"Invalid request parameter",
				"Invalid value for parameter '" + safeParameterName + "'");
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorResource> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
		return buildErrorResponse(
				HttpStatus.BAD_REQUEST,
				"INVALID_REQUEST",
				"Invalid request body",
				"Malformed JSON payload or invalid field value");
	}

	/**
	 * Handles validation exceptions from Spring's request body validation.
	 * Maps validation failure to a standardized error response.
	 *
	 * @param ex
	 *               the validation exception from @Valid binding
	 * @return error response with BAD_REQUEST status
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResource> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
		var fieldErrors = ex.getBindingResult().getFieldErrors();
		var validationPrefix = resolveMessageOrDefault("validation.field.prefix", "Field");
		var errorDetails = fieldErrors.isEmpty()
				? resolveMessageOrDefault("validation.request.failed", "Request validation failed")
				: fieldErrors.stream()
						.map(error -> "%s %s: %s".formatted(
								validationPrefix,
								error.getField(),
								error.getDefaultMessage()))
						.reduce((a, b) -> a + "; " + b)
						.orElse(resolveMessageOrDefault("validation.request.failed", "Request validation failed"));

		var applicationError = ApplicationError.validationError("request-body", errorDetails);
		return ErrorResponseAssembler.toErrorResponseFromApplicationError(applicationError);
	}

	/**
	 * Handles invalid request arguments such as malformed UUID path or payload
	 * values.
	 *
	 * @param ex
	 *               the illegal argument exception
	 * @return error response with BAD_REQUEST status
	 */
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ErrorResource> handleIllegalArgumentException(IllegalArgumentException ex) {
		var message = safeMessage(ex);
		var applicationError = ApplicationError.validationError(
				resolveMessageOrDefault("validation.request.argument", "request-argument"),
				message != null ? message
						: resolveMessageOrDefault("validation.request.failed", "Request validation failed"));
		return ErrorResponseAssembler.toErrorResponseFromApplicationError(applicationError);
	}

	/**
	 * Handles unexpected runtime exceptions not caught by specific handlers.
	 * Maps to a generic unexpected error response without exposing internal details.
	 *
	 * @param ex
	 *               the unhandled runtime exception
	 * @return error response with INTERNAL_SERVER_ERROR status
	 */
	@ExceptionHandler(RuntimeException.class)
	public ResponseEntity<ErrorResource> handleRuntimeException(RuntimeException ex) {
		log.error("Unhandled exception", ex);
		var applicationError = ApplicationError.unexpected(
				resolveMessageOrDefault("error.unexpected.context", "global-exception-handler"),
				"An unexpected internal error occurred");
		return ErrorResponseAssembler.toErrorResponseFromApplicationError(applicationError);
	}

	/**
	 * Handles all other exceptions not matched by specific handlers.
	 * Provides a final fallback for any unexpected exception type without exposing internal details.
	 *
	 * @param ex
	 *               the exception
	 * @return error response with INTERNAL_SERVER_ERROR status
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResource> handleException(Exception ex) {
		log.error("Unhandled exception", ex);
		var applicationError = ApplicationError.unexpected(
				resolveMessageOrDefault("error.unexpected.context", "global-exception-handler"),
				"An unexpected internal error occurred");
		return ErrorResponseAssembler.toErrorResponseFromApplicationError(applicationError);
	}

	private static ResponseEntity<ErrorResource> buildErrorResponse(
			HttpStatusCode status,
			String code,
			String message,
			Exception ex) {
		return buildErrorResponse(status, code, message, safeMessage(ex));
	}

	private static ResponseEntity<ErrorResource> buildErrorResponse(
			HttpStatusCode status,
			String code,
			String message,
			@Nullable String details) {
		return ResponseEntity.status(status).body(new ErrorResource(code, message, details));
	}

	private static @Nullable String safeMessage(Exception ex) {
		var message = ex.getMessage();
		return message == null || message.isBlank() ? null : message;
	}

	private String resolveMessageOrDefault(String key, String defaultValue, Object... args) {
		try {
			var bundle = ResourceBundle.getBundle(MESSAGES_BASENAME, LocaleContextHolder.getLocale());
			if (!bundle.containsKey(key)) {
				return defaultValue;
			}
			return MessageFormat.format(bundle.getString(key), args);
		} catch (MissingResourceException ex) {
			return defaultValue;
		}
	}
}
