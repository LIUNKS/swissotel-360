package com.swissotel.swissotel360.guests.interfaces.rest;

import com.swissotel.swissotel360.guests.application.queryservices.GuestQueryService;
import com.swissotel.swissotel360.guests.interfaces.rest.resources.DocumentTypeResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/document-types")
@Tag(name = "Document types", description = "Supported identity document types")
public class DocumentTypeController {
	private final GuestQueryService queryService;

	public DocumentTypeController(GuestQueryService queryService) {
		this.queryService = queryService;
	}

	@GetMapping
	@Operation(summary = "List supported document types", description = "Requires an authenticated user", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Document types listed"),
			@ApiResponse(responseCode = "401", description = "Authentication required") })
	public ResponseEntity<?> list() {
		return ResponseEntity.ok(queryService.getDocumentTypes().stream().map(DocumentTypeResource::from).toList());
	}
}
