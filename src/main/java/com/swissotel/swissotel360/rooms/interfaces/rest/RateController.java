package com.swissotel.swissotel360.rooms.interfaces.rest;

import com.swissotel.swissotel360.rooms.application.commandservices.RateCommandService;
import com.swissotel.swissotel360.rooms.application.queryservices.RateQueryService;
import com.swissotel.swissotel360.rooms.domain.model.queries.GetRatesByRoomTypeQuery;
import com.swissotel.swissotel360.rooms.interfaces.rest.resources.*;
import com.swissotel.swissotel360.rooms.interfaces.rest.transform.RateCommandAssembler;
import com.swissotel.swissotel360.rooms.interfaces.rest.transform.RoomsResourceAssembler;
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
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/room-types/{roomTypePublicId}/rates")
@Tag(name = "Room rates", description = "Room rate history and scheduling")
public class RateController {
	private final RateCommandService commands;
	private final RateQueryService queries;

	public RateController(RateCommandService commands, RateQueryService queries) {
		this.commands = commands;
		this.queries = queries;
	}

	@GetMapping
	@PreAuthorize("hasAuthority('rooms.view')")
	@Operation(summary = "List rates for a room type", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Rates ordered by start date"),
			@ApiResponse(responseCode = "404", description = "Room type not found") })
	public ResponseEntity<?> list(@PathVariable UUID roomTypePublicId) {
		return ResponseEntity.ok(queries.findByRoomType(new GetRatesByRoomTypeQuery(roomTypePublicId)).stream()
				.map(RoomsResourceAssembler::toResource).toList());
	}

	@PostMapping
	@PreAuthorize("hasAuthority('rates.manage')")
	@Operation(summary = "Create a room rate", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({ @ApiResponse(responseCode = "201", description = "Rate created"),
			@ApiResponse(responseCode = "400", description = "Invalid dates"),
			@ApiResponse(responseCode = "404", description = "Room type not found"),
			@ApiResponse(responseCode = "409", description = "Rate overlaps an existing rate") })
	public ResponseEntity<RateResource> create(@PathVariable UUID roomTypePublicId,
			@Valid @RequestBody CreateRateResource r) {
		var rate = commands.create(RateCommandAssembler.toCommand(roomTypePublicId, r));
		return ResponseEntity.status(HttpStatus.CREATED).body(RoomsResourceAssembler.toResource(rate));
	}

	@PatchMapping("/{ratePublicId}/close")
	@PreAuthorize("hasAuthority('rates.manage')")
	@Operation(summary = "Close an open rate", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Rate closed"),
			@ApiResponse(responseCode = "400", description = "Invalid end date"),
			@ApiResponse(responseCode = "404", description = "Rate not found") })
	public ResponseEntity<RateResource> close(@PathVariable UUID roomTypePublicId, @PathVariable UUID ratePublicId,
			@Valid @RequestBody CloseRateResource r) {
		return ResponseEntity.ok(RoomsResourceAssembler
				.toResource(commands.close(RateCommandAssembler.toCommand(roomTypePublicId, ratePublicId, r))));
	}

	@DeleteMapping("/{ratePublicId}")
	@PreAuthorize("hasAuthority('rates.manage')")
	@Operation(summary = "Delete a future rate", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({ @ApiResponse(responseCode = "204", description = "Rate deleted"),
			@ApiResponse(responseCode = "404", description = "Rate not found"),
			@ApiResponse(responseCode = "409", description = "Effective rates cannot be deleted") })
	public ResponseEntity<Void> delete(@PathVariable UUID roomTypePublicId, @PathVariable UUID ratePublicId) {
		commands.delete(RateCommandAssembler.toDeleteCommand(roomTypePublicId, ratePublicId));
		return ResponseEntity.noContent().build();
	}
}
