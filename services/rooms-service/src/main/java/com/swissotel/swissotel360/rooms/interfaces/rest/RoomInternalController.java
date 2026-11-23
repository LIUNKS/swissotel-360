package com.swissotel.swissotel360.rooms.interfaces.rest;

import com.swissotel.swissotel360.rooms.interfaces.acl.RoomInfoSnapshot;
import com.swissotel.swissotel360.rooms.interfaces.acl.RoomStatusSnapshot;
import com.swissotel.swissotel360.rooms.interfaces.acl.RoomsContextFacade;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/** Internal service-to-service endpoint exposed for downstream microservices (blocked at API Gateway). */
@Hidden
@RestController
@RequestMapping("/internal/rooms")
public class RoomInternalController {

	private final RoomsContextFacade rooms;

	public RoomInternalController(RoomsContextFacade rooms) {
		this.rooms = rooms;
	}

	@GetMapping("/{roomId}")
	public ResponseEntity<RoomInfoSnapshot> getById(@PathVariable Long roomId) {
		return rooms.findById(roomId).map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@GetMapping("/by-public-id/{roomPublicId}")
	public ResponseEntity<RoomInfoSnapshot> getByPublicId(@PathVariable UUID roomPublicId) {
		return rooms.findByPublicId(roomPublicId).map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@GetMapping("/available")
	public ResponseEntity<RoomInfoSnapshot> findFirstAvailable(@RequestParam Long roomTypeId) {
		return rooms.findFirstAvailable(roomTypeId).map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@PostMapping("/{roomId}/occupy")
	public ResponseEntity<RoomStatusSnapshot> markOccupied(@PathVariable Long roomId) {
		return ResponseEntity.ok(rooms.markOccupied(roomId));
	}

	@PostMapping("/{roomId}/check-out")
	public ResponseEntity<RoomStatusSnapshot> markCheckedOut(@PathVariable Long roomId) {
		return ResponseEntity.ok(rooms.markCheckedOut(roomId));
	}

	@PostMapping("/{roomId}/release")
	public ResponseEntity<RoomStatusSnapshot> releaseRoom(@PathVariable Long roomId) {
		return ResponseEntity.ok(rooms.releaseRoom(roomId));
	}
}
