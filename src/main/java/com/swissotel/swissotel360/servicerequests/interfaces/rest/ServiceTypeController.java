package com.swissotel.swissotel360.servicerequests.interfaces.rest;

import com.swissotel.swissotel360.servicerequests.application.queryservices.ServiceRequestQueryService;
import com.swissotel.swissotel360.servicerequests.interfaces.rest.resources.ServiceTypeResource;
import com.swissotel.swissotel360.servicerequests.interfaces.rest.transform.ServiceRequestRestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/v1/service-types")
@Tag(name = "Service types", description = "Catalog of requestable hotel services")
public class ServiceTypeController {
	private final ServiceRequestQueryService queries;

	public ServiceTypeController(ServiceRequestQueryService queries) {
		this.queries = queries;
	}

	@GetMapping
	@PreAuthorize("hasAuthority('service_requests.view')")
	@Operation(summary = "List service types", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Service types") })
	public ResponseEntity<List<ServiceTypeResource>> list() {
		return ResponseEntity
				.ok(queries.listServiceTypes().stream().map(ServiceRequestRestMapper::toResource).toList());
	}
}
