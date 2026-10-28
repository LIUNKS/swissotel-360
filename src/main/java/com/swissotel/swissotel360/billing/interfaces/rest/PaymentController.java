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
import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Payments", description = "Payment methods and folio payments")
public class PaymentController {
	private final BillingCommandService commands;
	private final BillingQueryService queries;

	public PaymentController(BillingCommandService commands, BillingQueryService queries) {
		this.commands = commands;
		this.queries = queries;
	}

	@GetMapping("/payment-methods")
	@PreAuthorize("hasAuthority('billing.view')")
	@Operation(summary = "List payment methods", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Payment methods") })
	public ResponseEntity<List<PaymentMethodResource>> methods() {
		return ResponseEntity.ok(queries.listPaymentMethods().stream().map(BillingRestMapper::toResource).toList());
	}

	@PostMapping("/payments")
	@PreAuthorize("hasAuthority('payments.process')")
	@Operation(summary = "Pay a folio in full", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({ @ApiResponse(responseCode = "201", description = "Payment processed"),
			@ApiResponse(responseCode = "404", description = "Folio or payment method not found"),
			@ApiResponse(responseCode = "409", description = "Folio closed or already paid"),
			@ApiResponse(responseCode = "422", description = "Amount does not match the folio total") })
	public ResponseEntity<PaymentResultResource> pay(@Valid @RequestBody CreatePaymentResource resource) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(BillingRestMapper.toResult(commands.processPayment(BillingRestMapper.toCommand(resource))));
	}
}