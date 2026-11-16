package com.swissotel.swissotel360.billing.interfaces.rest;

import com.swissotel.swissotel360.billing.application.commandservices.BillingCommandService;
import com.swissotel.swissotel360.billing.application.queryservices.BillingQueryService;
import com.swissotel.swissotel360.billing.interfaces.rest.resources.*;
import com.swissotel.swissotel360.billing.interfaces.rest.transform.BillingRestMapper;
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
@RequestMapping("/api/folios")
@Tag(name = "Folios", description = "Guest folios and charges")
public class FolioController {
	private final BillingCommandService commands;
	private final BillingQueryService queries;

	public FolioController(BillingCommandService commands, BillingQueryService queries) {
		this.commands = commands;
		this.queries = queries;
	}

	@GetMapping
	@PreAuthorize("hasAuthority('billing.view')")
	@Operation(summary = "Get the folio of a stay", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Folio found"),
			@ApiResponse(responseCode = "404", description = "Stay or folio not found") })
	public ResponseEntity<FolioResource> getByStay(@RequestParam UUID stayId) {
		return ResponseEntity.ok(BillingRestMapper.toResource(queries.getFolioByStay(stayId)));
	}

	@GetMapping("/{folioId}")
	@PreAuthorize("hasAuthority('billing.view')")
	@Operation(summary = "Get a folio", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Folio found"),
			@ApiResponse(responseCode = "404", description = "Folio not found") })
	public ResponseEntity<FolioResource> get(@PathVariable UUID folioId) {
		return ResponseEntity.ok(BillingRestMapper.toResource(queries.getFolio(folioId)));
	}

	@GetMapping("/{folioId}/summary")
	@PreAuthorize("hasAuthority('billing.view')")
	@Operation(summary = "Get a folio with its charges", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Folio summary"),
			@ApiResponse(responseCode = "404", description = "Folio not found") })
	public ResponseEntity<FolioSummaryResource> summary(@PathVariable UUID folioId) {
		return ResponseEntity.ok(BillingRestMapper.toSummary(queries.getFolio(folioId)));
	}

	@PostMapping("/{folioId}/charges")
	@PreAuthorize("hasAuthority('billing.charge')")
	@Operation(summary = "Add a charge to an open folio", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({ @ApiResponse(responseCode = "201", description = "Charge added"),
			@ApiResponse(responseCode = "404", description = "Folio or service request not found"),
			@ApiResponse(responseCode = "409", description = "Folio is closed") })
	public ResponseEntity<ChargeResultResource> addCharge(@PathVariable UUID folioId,
			@Valid @RequestBody AddChargeResource resource) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(BillingRestMapper.toResult(commands.addCharge(BillingRestMapper.toCommand(folioId, resource))));
	}
}