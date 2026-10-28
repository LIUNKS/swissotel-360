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

import org.jspecify.annotations.NullMarked;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Map;

/**
 * Global exception handler for REST API.
 * Provides centralized exception handling for the entire application,
 * ensuring all unhandled exceptions are translated to consistent
 * HTTP responses via the shared error assembly pattern.
 */
@RestControllerAdvice
@NullMarked
public class GlobalExceptionHandler {
    private static final String MESSAGES_BASENAME = "messages";

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResource> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResource("forbidden", "Permission denied", ex.getMessage()));
    }

    @ExceptionHandler(GuestNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleGuestNotFound(GuestNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "guest_not_found"));
    }

    @ExceptionHandler(DocumentTypeNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleDocumentTypeNotFound(DocumentTypeNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "document_type_not_found"));
    }

    @ExceptionHandler(DuplicateGuestException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateGuest(DuplicateGuestException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "error", "duplicate_document",
                "documentTypeId", ex.getDocumentTypeId(),
                "documentNumber", ex.getDocumentNumber()));
    }

    @ExceptionHandler(InvalidDocumentNumberException.class)
    public ResponseEntity<Map<String, String>> handleInvalidDocumentNumber(InvalidDocumentNumberException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", "invalid_document_number", "message", ex.getMessage()));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleUserNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "user_not_found"));
    }

    @ExceptionHandler(UserAlreadyLinkedException.class)
    public ResponseEntity<Map<String, String>> handleUserAlreadyLinked(UserAlreadyLinkedException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "user_already_linked"));
    }

    @ExceptionHandler(RoomTypeNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleRoomTypeNotFound(RoomTypeNotFoundException ex) { return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "room_type_not_found")); }

    @ExceptionHandler(RoomNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleRoomNotFound(RoomNotFoundException ex) { return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "room_not_found")); }

    @ExceptionHandler(RateEntityNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleRateEntityNotFound(RateEntityNotFoundException ex) { return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "rate_not_found")); }

    @ExceptionHandler(DuplicateRoomTypeException.class)
    public ResponseEntity<Map<String, String>> handleDuplicateRoomType(DuplicateRoomTypeException ex) { return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "duplicate_room_type")); }

    @ExceptionHandler(DuplicateRoomNumberException.class)
    public ResponseEntity<Map<String, String>> handleDuplicateRoomNumber(DuplicateRoomNumberException ex) { return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "duplicate_room_number")); }

    @ExceptionHandler(RateOverlapException.class)
    public ResponseEntity<Map<String, Object>> handleRateOverlap(RateOverlapException ex) { return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "rate_overlap", "conflicts", ex.getConflicts())); }

    @ExceptionHandler(InvalidRoomStatusTransitionException.class)
    public ResponseEntity<Map<String, String>> handleInvalidRoomStatus(InvalidRoomStatusTransitionException ex) { return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "invalid_status_transition", "from", ex.getFrom().name(), "to", ex.getTo().name())); }

    @ExceptionHandler(RoomTypeInUseException.class)
    public ResponseEntity<Map<String, String>> handleRoomTypeInUse(RoomTypeInUseException ex) { return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "room_type_in_use")); }

    @ExceptionHandler(RoomInUseException.class)
    public ResponseEntity<Map<String, String>> handleRoomInUse(RoomInUseException ex) { return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "room_in_use")); }

    @ExceptionHandler(OccupiedRoomTypeChangeException.class)
    public ResponseEntity<Map<String, String>> handleOccupiedRoomTypeChange(OccupiedRoomTypeChangeException ex) { return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "occupied_room_type_change")); }

    @ExceptionHandler(RateCannotBeDeletedException.class)
    public ResponseEntity<Map<String, String>> handleRateCannotBeDeleted(RateCannotBeDeletedException ex) { return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "rate_cannot_be_deleted")); }

    @ExceptionHandler(RateNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleRateNotFound(RateNotFoundException ex) { return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(Map.of("error", "rate_not_found", "date", ex.getDate().toString())); }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, org.springframework.http.converter.HttpMessageNotReadableException.class})
    public ResponseEntity<Map<String, String>> handleInvalidRequestFormat(Exception ex) { return ResponseEntity.badRequest().body(Map.of("error", "invalid_request", "message", ex.getMessage() == null ? "Invalid request value" : ex.getMessage())); }

    /**
     * Handles validation exceptions from Spring's request body validation.
     * Maps validation failure to a standardized error response.
     *
     * @param ex the validation exception from @Valid binding
     * @return error response with BAD_REQUEST status
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        var fieldErrors = ex.getBindingResult().getFieldErrors();
        var validationPrefix = resolveMessageOrDefault("validation.field.prefix", "Field");
        var errorDetails = fieldErrors.isEmpty()
                ? resolveMessageOrDefault("validation.request.failed", "Request validation failed")
                : fieldErrors.stream()
                        .map(error -> "%s %s: %s".formatted(
                                validationPrefix,
                                error.getField(),
                                error.getDefaultMessage()
                        ))
                        .reduce((a, b) -> a + "; " + b)
                        .orElse(resolveMessageOrDefault("validation.request.failed", "Request validation failed"));

        var applicationError = ApplicationError.validationError("request-body", errorDetails);
        return ErrorResponseAssembler.toErrorResponseFromApplicationError(applicationError);
    }

    /**
     * Handles invalid request arguments such as malformed UUID path or payload values.
     *
     * @param ex the illegal argument exception
     * @return error response with BAD_REQUEST status
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException ex) {
        var applicationError = ApplicationError.validationError(
                resolveMessageOrDefault("validation.request.argument", "request-argument"),
                ex.getMessage() != null ? ex.getMessage() : resolveMessageOrDefault("validation.request.failed", "Request validation failed")
        );
        return ErrorResponseAssembler.toErrorResponseFromApplicationError(applicationError);
    }

    /**
     * Handles unexpected runtime exceptions not caught by specific handlers.
     * Maps to a generic unexpected error response.
     *
     * @param ex the unhandled runtime exception
     * @return error response with INTERNAL_SERVER_ERROR status
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntimeException(RuntimeException ex) {
        var applicationError = ApplicationError.unexpected(
                resolveMessageOrDefault("error.unexpected.context", "global-exception-handler"),
                ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred"
        );
        return ErrorResponseAssembler.toErrorResponseFromApplicationError(applicationError);
    }

    /**
     * Handles all other exceptions not matched by specific handlers.
     * Provides a final fallback for any unexpected exception type.
     *
     * @param ex the exception
     * @return error response with INTERNAL_SERVER_ERROR status
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception ex) {
        var applicationError = ApplicationError.unexpected(
                resolveMessageOrDefault("error.unexpected.context", "global-exception-handler"),
                ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred"
        );
        return ErrorResponseAssembler.toErrorResponseFromApplicationError(applicationError);
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

