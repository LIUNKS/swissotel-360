package com.swissotel.swissotel360.billing.interfaces.rest;

import com.swissotel.swissotel360.billing.application.commandservices.BillingCommandService;
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

@RestController
@RequestMapping("/api/invoices")
@Tag(name = "Invoices", description = "Receipts and invoices")
public class InvoiceController {
	private final BillingCommandService commands;

	public InvoiceController(BillingCommandService commands) {
		this.commands = commands;
	}

	@PostMapping
	@PreAuthorize("hasAuthority('invoices.issue')")
	@Operation(summary = "Issue a receipt or invoice", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({ @ApiResponse(responseCode = "201", description = "Document issued"),
			@ApiResponse(responseCode = "404", description = "Payment not found"),
			@ApiResponse(responseCode = "409", description = "Payment not processed or document already issued") })
	public ResponseEntity<InvoiceResource> issue(@Valid @RequestBody IssueInvoiceResource resource) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(BillingRestMapper.toResource(commands.issueInvoice(BillingRestMapper.toCommand(resource))));
	}
}