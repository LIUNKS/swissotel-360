package com.swissotel.swissotel360.servicerequests.interfaces.rest;

import com.swissotel.swissotel360.servicerequests.domain.exceptions.InvalidServiceRequestTransitionException;
import com.swissotel.swissotel360.servicerequests.domain.exceptions.ServiceRequestNotFoundException;
import com.swissotel.swissotel360.servicerequests.domain.exceptions.ServiceRequestStayNotFoundException;
import com.swissotel.swissotel360.servicerequests.domain.exceptions.ServiceTypeNotFoundException;
import com.swissotel.swissotel360.servicerequests.domain.exceptions.StayNotActiveForRequestException;
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
public class ServiceRequestsExceptionHandler {

	@ExceptionHandler(ServiceRequestNotFoundException.class)
	public ResponseEntity<ErrorResource> handleServiceRequestNotFound(ServiceRequestNotFoundException ex) {
		return ErrorResponseAssembler.buildErrorResponse(HttpStatus.NOT_FOUND, "SERVICE_REQUEST_NOT_FOUND",
				"Service request not found", ex);
	}

	@ExceptionHandler(ServiceTypeNotFoundException.class)
	public ResponseEntity<ErrorResource> handleServiceTypeNotFound(ServiceTypeNotFoundException ex) {
		return ErrorResponseAssembler.buildErrorResponse(HttpStatus.NOT_FOUND, "SERVICE_TYPE_NOT_FOUND",
				"Service type not found", ex);
	}

	@ExceptionHandler(ServiceRequestStayNotFoundException.class)
	public ResponseEntity<ErrorResource> handleServiceRequestStayNotFound(ServiceRequestStayNotFoundException ex) {
		return ErrorResponseAssembler.buildErrorResponse(HttpStatus.NOT_FOUND, "STAY_NOT_FOUND", "Stay not found", ex);
	}

	@ExceptionHandler(StayNotActiveForRequestException.class)
	public ResponseEntity<ErrorResource> handleStayNotActiveForRequest(StayNotActiveForRequestException ex) {
		return ErrorResponseAssembler.buildErrorResponse(HttpStatus.CONFLICT, "STAY_NOT_ACTIVE",
				"Stay is not active", ex);
	}

	@ExceptionHandler(InvalidServiceRequestTransitionException.class)
	public ResponseEntity<ErrorResource> handleInvalidServiceRequestTransition(
			InvalidServiceRequestTransitionException ex) {
		return ErrorResponseAssembler.buildErrorResponse(HttpStatus.CONFLICT, "INVALID_SERVICE_REQUEST_TRANSITION",
				"Invalid service request transition", ex);
	}
}
