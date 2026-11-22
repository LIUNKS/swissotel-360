package com.swissotel.swissotel360.billing.interfaces.rest;

import com.swissotel.swissotel360.billing.application.command.AddChargeCommand;
import com.swissotel.swissotel360.billing.application.command.IssueInvoiceCommand;
import com.swissotel.swissotel360.billing.application.command.ProcessPaymentCommand;
import com.swissotel.swissotel360.billing.application.commandservices.BillingCommandService;
import com.swissotel.swissotel360.billing.application.dto.FolioView;
import com.swissotel.swissotel360.billing.application.queryservices.BillingQueryService;
import com.swissotel.swissotel360.billing.domain.aggregates.Folio;
import com.swissotel.swissotel360.billing.domain.aggregates.Invoice;
import com.swissotel.swissotel360.billing.domain.aggregates.Payment;
import com.swissotel.swissotel360.billing.domain.exceptions.BillingStayNotFoundException;
import com.swissotel.swissotel360.billing.domain.model.Charge;
import com.swissotel.swissotel360.billing.domain.model.DocumentType;
import com.swissotel.swissotel360.billing.domain.model.FolioId;
import com.swissotel.swissotel360.billing.domain.model.FolioStatus;
import com.swissotel.swissotel360.billing.domain.model.InvoiceId;
import com.swissotel.swissotel360.billing.domain.model.PaymentId;
import com.swissotel.swissotel360.billing.domain.model.PaymentMethod;
import com.swissotel.swissotel360.billing.domain.model.PaymentStatus;
import com.swissotel.swissotel360.shared.infrastructure.autoconfigure.SharedKernelAutoConfiguration;
import com.swissotel.swissotel360.shared.infrastructure.security.StatelessSecurityAutoConfiguration;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = { FolioController.class, PaymentController.class, InvoiceController.class })
@ImportAutoConfiguration({
		SecurityAutoConfiguration.class,
		ServletWebSecurityAutoConfiguration.class,
		SecurityFilterAutoConfiguration.class,
		StatelessSecurityAutoConfiguration.class,
		SharedKernelAutoConfiguration.class
})
@Import(BillingExceptionHandler.class)
@TestPropertySource(properties = "authorization.jwt.secret=swissotel-360-unit-test-jwt-secret-key-min-32-bytes")
class BillingRestSecurityWebTest {

	private static final String TEST_SECRET = "swissotel-360-unit-test-jwt-secret-key-min-32-bytes";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private BillingCommandService commands;

	@MockitoBean
	private BillingQueryService queries;

	@Test
	void rejectsUnauthenticatedRequestsAndInsufficientPermissions() throws Exception {
		mockMvc.perform(get("/api/v1/payment-methods"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("UNAUTHORIZED"));

		String guestToken = createJwt("ROLE_GUEST", List.of("reservations.view"));
		mockMvc.perform(get("/api/v1/payment-methods")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + guestToken))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("FORBIDDEN"));
	}

	@Test
	void exposesOnlyPublicUuidsAndCatalogCodesOnAuthorizedBillingRequests() throws Exception {
		var folioUuid = UUID.randomUUID();
		var stayUuid = UUID.randomUUID();
		var chargeUuid = UUID.randomUUID();
		var reqUuid = UUID.randomUUID();
		var paymentUuid = UUID.randomUUID();
		var invoiceUuid = UUID.randomUUID();
		var now = Instant.parse("2026-11-22T18:00:00Z");

		var charge = new Charge(5L, chargeUuid, 77L, "Minibar", new BigDecimal("35.00"), now);
		var folio = Folio.restore(new FolioId(folioUuid), 10L, 42L, FolioStatus.OPEN, List.of(charge));
		var view = new FolioView(folio, stayUuid, Map.of(77L, reqUuid));
		var method = new PaymentMethod(1L, "cash", "Efectivo");
		var payment = Payment.restore(new PaymentId(paymentUuid), 20L, 10L, method, new BigDecimal("35.00"),
				PaymentStatus.PROCESSED, "SIM-ABC123", now);
		var invoice = Invoice.restore(new InvoiceId(invoiceUuid), 30L, 20L, DocumentType.RECEIPT,
				"B001", "00000001", now, new BigDecimal("35.00"));

		when(queries.listPaymentMethods()).thenReturn(List.of(method));
		when(queries.getFolio(folioUuid)).thenReturn(view);
		when(queries.getFolioByStay(stayUuid)).thenThrow(new BillingStayNotFoundException(stayUuid));
		when(commands.addCharge(any(AddChargeCommand.class))).thenReturn(charge);
		when(commands.processPayment(any(ProcessPaymentCommand.class))).thenReturn(payment);
		when(commands.issueInvoice(any(IssueInvoiceCommand.class))).thenReturn(invoice);

		String staffJwt = createJwt("ROLE_RECEPTIONIST",
				List.of("billing.view", "billing.charge", "payments.process", "invoices.issue"));

		mockMvc.perform(get("/api/v1/payment-methods").header(HttpHeaders.AUTHORIZATION, "Bearer " + staffJwt))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].code").value("cash"))
				.andExpect(jsonPath("$..id").isEmpty());

		mockMvc.perform(get("/api/v1/folios/{id}/summary", folioUuid)
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + staffJwt))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.folioId").value(folioUuid.toString()))
				.andExpect(jsonPath("$.charges[0].chargeId").value(chargeUuid.toString()))
				.andExpect(jsonPath("$.charges[0].serviceRequestId").value(reqUuid.toString()))
				.andExpect(jsonPath("$..id").isEmpty());

		mockMvc.perform(get("/api/v1/folios").param("stayId", stayUuid.toString())
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + staffJwt))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("STAY_NOT_FOUND"));

		mockMvc.perform(post("/api/v1/folios/{id}/charges", folioUuid)
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + staffJwt)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"concept":"Minibar","amount":35.00,"serviceRequestId":"%s"}"""
								.formatted(reqUuid)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.chargeId").value(chargeUuid.toString()))
				.andExpect(jsonPath("$..id").isEmpty());

		mockMvc.perform(post("/api/v1/payments")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + staffJwt)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"folioId":"%s","paymentMethodCode":"cash","amount":35.00}"""
								.formatted(folioUuid)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.paymentId").value(paymentUuid.toString()))
				.andExpect(jsonPath("$..id").isEmpty());

		mockMvc.perform(post("/api/v1/invoices")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + staffJwt)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"paymentId":"%s","documentType":"receipt"}"""
								.formatted(paymentUuid)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.invoiceId").value(invoiceUuid.toString()))
				.andExpect(jsonPath("$..id").isEmpty());
	}

	private static String createJwt(String role, List<String> permissions) {
		var key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
		return Jwts.builder()
				.subject("staff@swissotel.com")
				.claim("uid", 9L)
				.claim("pid", UUID.randomUUID().toString())
				.claim("role", role)
				.claim("perms", permissions)
				.issuedAt(new Date())
				.expiration(new Date(System.currentTimeMillis() + 60_000))
				.signWith(key)
				.compact();
	}
}
