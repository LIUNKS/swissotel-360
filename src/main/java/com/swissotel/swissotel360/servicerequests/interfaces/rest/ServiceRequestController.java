package com.swissotel.swissotel360.servicerequests.interfaces.rest;

import com.swissotel.swissotel360.servicerequests.application.commandservices.ServiceRequestCommandService;
import com.swissotel.swissotel360.servicerequests.application.queryservices.ServiceRequestQueryService;
import com.swissotel.swissotel360.servicerequests.domain.model.ServiceRequestStatus;
import com.swissotel.swissotel360.servicerequests.interfaces.rest.resources.*;
import com.swissotel.swissotel360.servicerequests.interfaces.rest.transform.ServiceRequestRestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/service-requests")
@Tag(name = "Service requests", description = "Guest service requests during a stay")
public class ServiceRequestController {
	private final ServiceRequestCommandService commands;
	private final ServiceRequestQueryService queries;

	public ServiceRequestController(ServiceRequestCommandService commands, ServiceRequestQueryService queries) {
		this.commands = commands;
		this.queries = queries;
	}

	@PostMapping
	@PreAuthorize("hasAuthority('service_requests.create')")
	@Operation(summary = "Register a service request", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({ @ApiResponse(responseCode = "201", description = "Request registered"),
			@ApiResponse(responseCode = "404", description = "Stay or service type not found"),
			@ApiResponse(responseCode = "409", description = "Stay is not active") })
	public ResponseEntity<CreateServiceRequestResultResource> create(
			@Valid @RequestBody CreateServiceRequestResource resource) {
		return ResponseEntity.status(HttpStatus.CREATED).body(ServiceRequestRestMapper
				.toCreated(commands.create(ServiceRequestRestMapper.toCommand(resource))));
	}

	@GetMapping
	@PreAuthorize("hasAuthority('service_requests.view')")
	@Operation(summary = "List service requests", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Service requests"),
			@ApiResponse(responseCode = "400", description = "Invalid status filter"),
			@ApiResponse(responseCode = "404", description = "Stay not found") })
	public ResponseEntity<List<ServiceRequestResource>> list(@RequestParam(required = false) UUID stayId,
			@RequestParam(required = false) String status) {
		var parsed = status == null ? null : ServiceRequestStatus.fromCode(status);
		return ResponseEntity
				.ok(queries.list(stayId, parsed).stream().map(ServiceRequestRestMapper::toResource).toList());
	}

	@GetMapping("/{requestId}")
	@PreAuthorize("hasAuthority('service_requests.view')")
	@Operation(summary = "Get a service request", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Request found"),
			@ApiResponse(responseCode = "404", description = "Request not found") })
	public ResponseEntity<ServiceRequestResource> get(@PathVariable UUID requestId) {
		return ResponseEntity.ok(ServiceRequestRestMapper.toResource(queries.getView(requestId)));
	}

	@PatchMapping("/{requestId}/status")
	@PreAuthorize("hasAuthority('service_requests.handle')")
	@Operation(summary = "Update a request status", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Status updated"),
			@ApiResponse(responseCode = "404", description = "Request not found"),
			@ApiResponse(responseCode = "409", description = "Invalid status transition") })
	public ResponseEntity<ServiceRequestStatusResource> updateStatus(@PathVariable UUID requestId,
			@Valid @RequestBody UpdateServiceRequestStatusResource resource) {
		return ResponseEntity.ok(ServiceRequestRestMapper
				.toStatus(commands.updateStatus(ServiceRequestRestMapper.toCommand(requestId, resource))));
	}
}
