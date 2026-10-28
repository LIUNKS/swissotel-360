package com.swissotel.swissotel360.stays.interfaces.rest;

import com.swissotel.swissotel360.stays.application.commandservices.StayCommandService;
import com.swissotel.swissotel360.stays.application.queryservices.StayQueryService;
import com.swissotel.swissotel360.stays.domain.model.StayStatus;
import com.swissotel.swissotel360.stays.interfaces.rest.resources.*;
import com.swissotel.swissotel360.stays.interfaces.rest.transform.StayRestMapper;
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
@RequestMapping("/api/stays")
@Tag(name = "Stays", description = "Guest check-in and check-out")
public class StayController {
	private final StayCommandService commands;
	private final StayQueryService queries;

	public StayController(StayCommandService commands, StayQueryService queries) {
		this.commands = commands;
		this.queries = queries;
	}

	@PostMapping("/check-in")
	@PreAuthorize("hasAuthority('stays.checkin')")
	@Operation(summary = "Check a guest in", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({ @ApiResponse(responseCode = "201", description = "Stay opened and digital key issued"),
			@ApiResponse(responseCode = "404", description = "Reservation not found"),
			@ApiResponse(responseCode = "409", description = "Reservation not eligible or room not available"),
			@ApiResponse(responseCode = "422", description = "Document does not match the reservation guest") })
	public ResponseEntity<CheckInResultResource> checkIn(@Valid @RequestBody CheckInStayResource resource) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(StayRestMapper.toCheckIn(commands.checkIn(StayRestMapper.toCommand(resource))));
	}

	@GetMapping
	@PreAuthorize("hasAnyAuthority('stays.checkin','stays.checkout')")
	@Operation(summary = "List stays", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Stays"),
			@ApiResponse(responseCode = "400", description = "Invalid status filter") })
	public ResponseEntity<List<StayResource>> list(@RequestParam(required = false) String status) {
		var parsed = status == null ? null : StayStatus.fromCode(status);
		return ResponseEntity.ok(queries.list(parsed).stream().map(StayRestMapper::toResource).toList());
	}

	@GetMapping("/{stayId}")
	@PreAuthorize("hasAnyAuthority('stays.checkin','stays.checkout')")
	@Operation(summary = "Get a stay", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Stay found"),
			@ApiResponse(responseCode = "404", description = "Stay not found") })
	public ResponseEntity<StayResource> get(@PathVariable UUID stayId) {
		return ResponseEntity.ok(StayRestMapper.toResource(queries.getView(stayId)));
	}

	@PostMapping("/{stayId}/check-out")
	@PreAuthorize("hasAuthority('stays.checkout')")
	@Operation(summary = "Check a guest out", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Stay closed and keys revoked"),
			@ApiResponse(responseCode = "404", description = "Stay not found"),
			@ApiResponse(responseCode = "409", description = "Stay already closed") })
	public ResponseEntity<CheckOutResource> checkOut(@PathVariable UUID stayId) {
		return ResponseEntity.ok(StayRestMapper.toCheckOut(commands.checkOut(StayRestMapper.checkOut(stayId))));
	}
}
