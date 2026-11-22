package com.swissotel.swissotel360.shared.interfaces.rest;

import com.swissotel.swissotel360.shared.interfaces.rest.resources.ErrorResource;
import com.swissotel.swissotel360.shared.interfaces.rest.transform.ErrorResponseAssembler;
import org.jspecify.annotations.NullMarked;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Translates Spring Security access denial exceptions into a uniform 403 ErrorResource
 * without leaking policy or internal authorization details.
 */
@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE - 10)
@NullMarked
public class SecurityExceptionHandler {

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ErrorResource> handleAccessDenied(AccessDeniedException ex) {
		return ErrorResponseAssembler.buildErrorResponse(
				HttpStatus.FORBIDDEN,
				"FORBIDDEN",
				"Permission denied",
				"Access denied");
	}
}
