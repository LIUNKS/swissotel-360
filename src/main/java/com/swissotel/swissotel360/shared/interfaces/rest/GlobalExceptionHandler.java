package com.swissotel.swissotel360.shared.interfaces.rest;

import com.swissotel.swissotel360.shared.application.result.ApplicationError;
import com.swissotel.swissotel360.shared.interfaces.rest.resources.ErrorResource;
import com.swissotel.swissotel360.shared.interfaces.rest.transform.ErrorResponseAssembler;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Global exception handler for REST API.
 * Provides centralized handling of generic HTTP, validation, security, and
 * fallback exceptions across the application without coupling shared to any
 * bounded context.
 */
@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
@NullMarked
public class GlobalExceptionHandler {
	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
	private static final String MESSAGES_BASENAME = "messages";

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ErrorResource> handleAccessDenied(AccessDeniedException ex) {
		return ErrorResponseAssembler.buildErrorResponse(HttpStatus.FORBIDDEN, "FORBIDDEN", "Permission denied",
				"Access denied");
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ErrorResource> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
		var parameterName = ex.getName();
		var safeParameterName = parameterName != null && !parameterName.isBlank() ? parameterName : "unknown";
		return ErrorResponseAssembler.buildErrorResponse(
				HttpStatus.BAD_REQUEST,
				"INVALID_REQUEST",
				"Invalid request parameter",
				"Invalid value for parameter '" + safeParameterName + "'");
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorResource> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
		return ErrorResponseAssembler.buildErrorResponse(
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
		var message = ErrorResponseAssembler.safeMessage(ex);
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
