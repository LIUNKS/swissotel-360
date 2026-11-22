package com.swissotel.swissotel360.billing.interfaces.rest;

import com.swissotel.swissotel360.billing.domain.exceptions.BillingStayNotFoundException;
import com.swissotel.swissotel360.billing.domain.exceptions.ChargeServiceRequestNotFoundException;
import com.swissotel.swissotel360.billing.domain.exceptions.FolioAlreadyPaidException;
import com.swissotel.swissotel360.billing.domain.exceptions.FolioClosedException;
import com.swissotel.swissotel360.billing.domain.exceptions.FolioNotFoundException;
import com.swissotel.swissotel360.billing.domain.exceptions.InvoiceAlreadyIssuedException;
import com.swissotel.swissotel360.billing.domain.exceptions.PaymentAmountMismatchException;
import com.swissotel.swissotel360.billing.domain.exceptions.PaymentMethodNotFoundException;
import com.swissotel.swissotel360.billing.domain.exceptions.PaymentNotFoundException;
import com.swissotel.swissotel360.billing.domain.exceptions.PaymentNotProcessedException;
import com.swissotel.swissotel360.billing.domain.model.DocumentType;
import com.swissotel.swissotel360.billing.domain.model.FolioStatus;
import com.swissotel.swissotel360.billing.domain.model.PaymentStatus;
import com.swissotel.swissotel360.shared.interfaces.rest.resources.ErrorResource;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BillingExceptionHandlerTest {

	private final BillingExceptionHandler handler = new BillingExceptionHandler();

	@Test
	void mapsBillingExceptionsToExpectedStatusesAndErrorResources() {
		var uuid = UUID.randomUUID();

		assertErrorResponse(handler.handleFolioNotFound(new FolioNotFoundException(uuid)),
				404, "FOLIO_NOT_FOUND", "Folio not found", uuid.toString());
		assertErrorResponse(handler.handlePaymentMethodNotFound(new PaymentMethodNotFoundException("CARD")),
				404, "PAYMENT_METHOD_NOT_FOUND", "Payment method not found", "CARD");
		assertErrorResponse(handler.handlePaymentNotFound(new PaymentNotFoundException(uuid)),
				404, "PAYMENT_NOT_FOUND", "Payment not found", uuid.toString());
		assertErrorResponse(handler.handleChargeServiceRequestNotFound(new ChargeServiceRequestNotFoundException(uuid)),
				404, "SERVICE_REQUEST_NOT_FOUND", "Service request not found", uuid.toString());
		assertErrorResponse(handler.handleBillingStayNotFound(new BillingStayNotFoundException(uuid)),
				404, "STAY_NOT_FOUND", "Stay not found", uuid.toString());

		assertErrorResponse(handler.handleFolioClosed(new FolioClosedException()),
				409, "FOLIO_CLOSED", "Folio is closed", "closed");
		assertErrorResponse(handler.handleFolioAlreadyPaid(new FolioAlreadyPaidException()),
				409, "FOLIO_ALREADY_PAID", "Folio is already paid", "payment");
		assertErrorResponse(handler.handlePaymentNotProcessed(new PaymentNotProcessedException()),
				409, "PAYMENT_NOT_PROCESSED", "Payment is not processed", "not processed");
		assertErrorResponse(handler.handleInvoiceAlreadyIssued(new InvoiceAlreadyIssuedException()),
				409, "INVOICE_ALREADY_ISSUED", "Invoice already issued", "already issued");

		assertErrorResponse(handler.handlePaymentAmountMismatch(new PaymentAmountMismatchException()),
				422, "PAYMENT_AMOUNT_MISMATCH", "Payment amount mismatch", "folio total");
	}

	@Test
	void domainEnumsRejectInvalidCodesWithoutExposingJavaClassNames() {
		assertInvalidEnumCode(() -> DocumentType.fromCode("zzz"), "Invalid document type");
		assertInvalidEnumCode(() -> DocumentType.fromCode(null), "Invalid document type");
		assertInvalidEnumCode(() -> DocumentType.fromCode("   "), "Invalid document type");
		assertEquals(DocumentType.RECEIPT, DocumentType.fromCode("receipt"));

		assertInvalidEnumCode(() -> FolioStatus.fromCode("zzz"), "Invalid folio status");
		assertInvalidEnumCode(() -> FolioStatus.fromCode(null), "Invalid folio status");
		assertInvalidEnumCode(() -> FolioStatus.fromCode("   "), "Invalid folio status");
		assertEquals(FolioStatus.OPEN, FolioStatus.fromCode("open"));

		assertInvalidEnumCode(() -> PaymentStatus.fromCode("zzz"), "Invalid payment status");
		assertInvalidEnumCode(() -> PaymentStatus.fromCode(null), "Invalid payment status");
		assertInvalidEnumCode(() -> PaymentStatus.fromCode("   "), "Invalid payment status");
		assertEquals(PaymentStatus.PROCESSED, PaymentStatus.fromCode("processed"));
	}

	private static void assertErrorResponse(
			ResponseEntity<ErrorResource> response,
			int expectedStatus,
			String expectedCode,
			String expectedMessage,
			String expectedDetailsSubstring) {
		assertEquals(expectedStatus, response.getStatusCode().value());
		var body = response.getBody();
		assertNotNull(body);
		assertEquals(expectedCode, body.code());
		assertEquals(expectedMessage, body.message());
		assertNotNull(body.details());
		assertTrue(body.details().contains(expectedDetailsSubstring));
		for (var value : List.of(body.code(), body.message(), body.details())) {
			assertFalse(value.contains("com.swissotel"));
			assertFalse(value.contains("java."));
			assertFalse(value.contains("org."));
		}
	}

	private static void assertInvalidEnumCode(Runnable call, String expectedPrefix) {
		var ex = assertThrows(IllegalArgumentException.class, call::run);
		assertNotNull(ex.getMessage());
		assertTrue(ex.getMessage().startsWith(expectedPrefix));
		assertFalse(ex.getMessage().contains("com.swissotel"));
		assertFalse(ex.getMessage().contains("java."));
		assertFalse(ex.getMessage().contains("No enum constant"));
	}
}
