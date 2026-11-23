package com.swissotel.swissotel360.stays.interfaces.rest;

import com.swissotel.swissotel360.stays.interfaces.acl.StayContextSnapshot;
import com.swissotel.swissotel360.stays.interfaces.acl.StaysContextFacade;
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
@RequestMapping("/internal/stays")
public class StayInternalController {

	private final StaysContextFacade stays;

	public StayInternalController(StaysContextFacade stays) {
		this.stays = stays;
	}

	@GetMapping("/{stayId}")
	public ResponseEntity<StayContextSnapshot> getById(@PathVariable Long stayId) {
		return stays.findById(stayId).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
	}

	@GetMapping("/by-public-id/{stayPublicId}")
	public ResponseEntity<StayContextSnapshot> getByPublicId(@PathVariable UUID stayPublicId) {
		return stays.findByPublicId(stayPublicId).map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.notFound().build());
	}
}
