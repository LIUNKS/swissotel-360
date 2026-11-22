package com.swissotel.swissotel360.stays.interfaces.rest;

import com.swissotel.swissotel360.stays.application.commandservices.StayCommandService;
import com.swissotel.swissotel360.stays.application.command.RevokeDigitalKeyCommand;
import com.swissotel.swissotel360.stays.interfaces.rest.resources.DigitalKeyResource;
import com.swissotel.swissotel360.stays.interfaces.rest.resources.IssueDigitalKeyResource;
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
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/digital-keys")
@Tag(name = "Digital keys", description = "Digital room keys of active stays")
public class DigitalKeyController {
	private final StayCommandService commands;

	public DigitalKeyController(StayCommandService commands) {
		this.commands = commands;
	}

	@PostMapping
	@PreAuthorize("hasAuthority('keys.manage')")
	@Operation(summary = "Issue a new digital key (revokes the previous one)", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({ @ApiResponse(responseCode = "201", description = "Key issued"),
			@ApiResponse(responseCode = "404", description = "Stay not found"),
			@ApiResponse(responseCode = "409", description = "Stay already closed") })
	public ResponseEntity<DigitalKeyResource> issue(@Valid @RequestBody IssueDigitalKeyResource resource) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(StayRestMapper.toIssued(commands.issueKey(StayRestMapper.toCommand(resource))));
	}

	@PostMapping("/{keyId}/revocation")
	@PreAuthorize("hasAuthority('keys.manage')")
	@Operation(summary = "Revoke a digital key", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Key revoked"),
			@ApiResponse(responseCode = "404", description = "Key not found") })
	public ResponseEntity<DigitalKeyResource> revoke(@PathVariable UUID keyId) {
		return ResponseEntity.ok(StayRestMapper.toRevoked(commands.revokeKey(new RevokeDigitalKeyCommand(keyId))));
	}
}
