package com.swissotel.swissotel360.guests.interfaces.rest;

import com.swissotel.swissotel360.guests.interfaces.acl.GuestDirectoryContextFacade;
import com.swissotel.swissotel360.guests.interfaces.acl.GuestDirectorySnapshot;
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
@RequestMapping("/internal/guests")
public class GuestInternalController {

	private final GuestDirectoryContextFacade guests;

	public GuestInternalController(GuestDirectoryContextFacade guests) {
		this.guests = guests;
	}

	@GetMapping("/{guestId}")
	public ResponseEntity<GuestDirectorySnapshot> getById(@PathVariable Long guestId) {
		return guests.findById(guestId).map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@GetMapping("/by-public-id/{guestPublicId}")
	public ResponseEntity<GuestDirectorySnapshot> getByPublicId(@PathVariable UUID guestPublicId) {
		return guests.findByPublicId(guestPublicId).map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@GetMapping("/by-user-id/{userId}")
	public ResponseEntity<GuestDirectorySnapshot> getByUserId(@PathVariable Long userId) {
		return guests.findByUserId(userId).map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.notFound().build());
	}
}
