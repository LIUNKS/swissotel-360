package com.swissotel.swissotel360.rooms.interfaces.rest;

import com.swissotel.swissotel360.rooms.application.commandservices.RoomCommandService;
import com.swissotel.swissotel360.rooms.application.queryservices.RoomQueryService;
import com.swissotel.swissotel360.rooms.domain.model.queries.RoomFilter;
import com.swissotel.swissotel360.rooms.domain.model.queries.GetRoomByPublicIdQuery;
import com.swissotel.swissotel360.rooms.domain.model.valueobjects.RoomStatus;
import com.swissotel.swissotel360.rooms.interfaces.rest.resources.CreateRoomResource;
import com.swissotel.swissotel360.rooms.interfaces.rest.resources.RoomResource;
import com.swissotel.swissotel360.rooms.interfaces.rest.resources.UpdateRoomResource;
import com.swissotel.swissotel360.rooms.interfaces.rest.resources.UpdateRoomStatusResource;
import com.swissotel.swissotel360.rooms.interfaces.rest.transform.RoomCommandAssembler;
import com.swissotel.swissotel360.rooms.interfaces.rest.transform.RoomsResourceAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/rooms")
@Tag(name = "Rooms", description = "Physical room inventory and status management")
public class RoomController {
	private final RoomCommandService commands;
	private final RoomQueryService queries;

	public RoomController(RoomCommandService commands, RoomQueryService queries) {
		this.commands = commands;
		this.queries = queries;
	}

	@GetMapping
	@PreAuthorize("hasAuthority('rooms.view')")
	@Operation(summary = "Search rooms", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Paged rooms"),
			@ApiResponse(responseCode = "400", description = "Invalid pagination") })
	public ResponseEntity<?> list(@RequestParam(required = false) Long roomTypeId,
			@RequestParam(required = false) RoomStatus status,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		if (page < 0 || size < 1 || size > 100) {
			throw new IllegalArgumentException("page must be non-negative and size must be between 1 and 100");
		}
		var result = queries.find(new RoomFilter(roomTypeId, status, page, size));
		var content = result.content().stream().map(RoomsResourceAssembler::toResource).toList();
		return ResponseEntity.ok(new PageImpl<>(content, PageRequest.of(page, size), result.totalElements()));
	}

	@GetMapping("/{publicId}")
	@PreAuthorize("hasAuthority('rooms.view')")
	@Operation(summary = "Get a room", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Room found"),
			@ApiResponse(responseCode = "404", description = "Room not found") })
	public ResponseEntity<RoomResource> get(@PathVariable UUID publicId) {
		return ResponseEntity
				.ok(RoomsResourceAssembler.toResource(queries.findByPublicId(new GetRoomByPublicIdQuery(publicId))));
	}

	@PostMapping
	@PreAuthorize("hasAuthority('rooms.manage')")
	@Operation(summary = "Create a room", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({ @ApiResponse(responseCode = "201", description = "Room created"),
			@ApiResponse(responseCode = "404", description = "Room type not found"),
			@ApiResponse(responseCode = "409", description = "Duplicate room number") })
	public ResponseEntity<RoomResource> create(@Valid @RequestBody CreateRoomResource resource) {
		var room = commands.create(RoomCommandAssembler.toCommand(resource));
		var view = queries.findByPublicId(new GetRoomByPublicIdQuery(room.publicId()));
		return ResponseEntity.status(HttpStatus.CREATED).body(RoomsResourceAssembler.toResource(view));
	}

	@PutMapping("/{publicId}")
	@PreAuthorize("hasAuthority('rooms.manage')")
	@Operation(summary = "Update a room", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Room updated"),
			@ApiResponse(responseCode = "404", description = "Room or room type not found"),
			@ApiResponse(responseCode = "409", description = "Invalid room update") })
	public ResponseEntity<RoomResource> update(@PathVariable UUID publicId,
			@Valid @RequestBody UpdateRoomResource resource) {
		commands.update(RoomCommandAssembler.toCommand(publicId, resource));
		return ResponseEntity
				.ok(RoomsResourceAssembler.toResource(queries.findByPublicId(new GetRoomByPublicIdQuery(publicId))));
	}

	@PatchMapping("/{publicId}/status")
	@PreAuthorize("@roomStatusAuthorizationService.canUpdate(authentication, #publicId, #resource.status)")
	@Operation(summary = "Update a room status", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Room status updated"),
			@ApiResponse(responseCode = "403", description = "Role cannot perform this transition"),
			@ApiResponse(responseCode = "409", description = "Invalid room status transition") })
	public ResponseEntity<RoomResource> updateStatus(@PathVariable UUID publicId,
			@Valid @RequestBody UpdateRoomStatusResource resource) {
		commands.updateStatus(RoomCommandAssembler.toStatusCommand(publicId, resource));
		return ResponseEntity
				.ok(RoomsResourceAssembler.toResource(queries.findByPublicId(new GetRoomByPublicIdQuery(publicId))));
	}
}
