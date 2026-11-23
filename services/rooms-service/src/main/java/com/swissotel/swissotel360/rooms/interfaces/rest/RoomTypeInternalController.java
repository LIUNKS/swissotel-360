package com.swissotel.swissotel360.rooms.interfaces.rest;

import com.swissotel.swissotel360.rooms.interfaces.acl.AvailabilityContextFacade;
import com.swissotel.swissotel360.rooms.interfaces.acl.RoomAvailabilitySnapshot;
import com.swissotel.swissotel360.rooms.interfaces.acl.RoomTypeInventorySnapshot;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

/** Internal service-to-service endpoint exposed for downstream microservices (blocked at API Gateway). */
@Hidden
@RestController
@RequestMapping("/internal/room-types")
public class RoomTypeInternalController {

	private final AvailabilityContextFacade availability;

	public RoomTypeInternalController(AvailabilityContextFacade availability) {
		this.availability = availability;
	}

	@GetMapping("/{roomTypeId}")
	public ResponseEntity<RoomTypeInventorySnapshot> getById(@PathVariable Long roomTypeId) {
		return availability.findRoomTypeById(roomTypeId).map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@GetMapping("/by-public-id/{roomTypePublicId}")
	public ResponseEntity<RoomTypeInventorySnapshot> getByPublicId(@PathVariable UUID roomTypePublicId) {
		return availability.findRoomType(roomTypePublicId).map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@GetMapping("/{roomTypeId}/sellable-units")
	public ResponseEntity<SellableUnitsResponse> sellableUnits(@PathVariable Long roomTypeId) {
		return ResponseEntity.ok(new SellableUnitsResponse(availability.sellableUnits(roomTypeId)));
	}

	@GetMapping("/{roomTypeId}/quote")
	public ResponseEntity<RoomAvailabilitySnapshot> quote(
			@PathVariable Long roomTypeId,
			@RequestParam LocalDate checkIn,
			@RequestParam LocalDate checkOut) {
		return ResponseEntity.ok(availability.quote(roomTypeId, checkIn, checkOut));
	}

	public record SellableUnitsResponse(int sellableUnits) {
	}
}
