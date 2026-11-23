package com.swissotel.swissotel360.servicerequests.interfaces.rest;

import com.swissotel.swissotel360.servicerequests.interfaces.acl.ServiceRequestContextSnapshot;
import com.swissotel.swissotel360.servicerequests.interfaces.acl.ServiceRequestsContextFacade;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/** Internal service-to-service endpoint exposed for downstream microservices (blocked at API Gateway). */
@Hidden
@RestController
@RequestMapping("/internal/service-requests")
public class ServiceRequestInternalController {

	private final ServiceRequestsContextFacade serviceRequests;

	public ServiceRequestInternalController(ServiceRequestsContextFacade serviceRequests) {
		this.serviceRequests = serviceRequests;
	}

	@GetMapping("/{requestId}")
	public ResponseEntity<ServiceRequestContextSnapshot> getById(@PathVariable Long requestId) {
		return serviceRequests.findById(requestId).map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@GetMapping("/by-public-id/{requestPublicId}")
	public ResponseEntity<ServiceRequestContextSnapshot> getByPublicId(@PathVariable UUID requestPublicId) {
		return serviceRequests.findByPublicId(requestPublicId).map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.notFound().build());
	}
}
