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
import com.swissotel.swissotel360.shared.interfaces.rest.resources.ErrorResource;
import com.swissotel.swissotel360.shared.interfaces.rest.transform.ErrorResponseAssembler;
import org.jspecify.annotations.NullMarked;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
@NullMarked
public class BillingExceptionHandler {

	@ExceptionHandler(FolioNotFoundException.class)
	public ResponseEntity<ErrorResource> handleFolioNotFound(FolioNotFoundException ex) {
		return ErrorResponseAssembler.buildErrorResponse(HttpStatus.NOT_FOUND, "FOLIO_NOT_FOUND", "Folio not found", ex);
	}

	@ExceptionHandler(FolioClosedException.class)
	public ResponseEntity<ErrorResource> handleFolioClosed(FolioClosedException ex) {
		return ErrorResponseAssembler.buildErrorResponse(HttpStatus.CONFLICT, "FOLIO_CLOSED", "Folio is closed", ex);
	}

	@ExceptionHandler(PaymentMethodNotFoundException.class)
	public ResponseEntity<ErrorResource> handlePaymentMethodNotFound(PaymentMethodNotFoundException ex) {
		return ErrorResponseAssembler.buildErrorResponse(HttpStatus.NOT_FOUND, "PAYMENT_METHOD_NOT_FOUND",
				"Payment method not found", ex);
	}

	@ExceptionHandler(PaymentAmountMismatchException.class)
	public ResponseEntity<ErrorResource> handlePaymentAmountMismatch(PaymentAmountMismatchException ex) {
		return ErrorResponseAssembler.buildErrorResponse(HttpStatus.UNPROCESSABLE_CONTENT, "PAYMENT_AMOUNT_MISMATCH",
				"Payment amount mismatch", ex);
	}

	@ExceptionHandler(FolioAlreadyPaidException.class)
	public ResponseEntity<ErrorResource> handleFolioAlreadyPaid(FolioAlreadyPaidException ex) {
		return ErrorResponseAssembler.buildErrorResponse(HttpStatus.CONFLICT, "FOLIO_ALREADY_PAID",
				"Folio is already paid", ex);
	}

	@ExceptionHandler(PaymentNotFoundException.class)
	public ResponseEntity<ErrorResource> handlePaymentNotFound(PaymentNotFoundException ex) {
		return ErrorResponseAssembler.buildErrorResponse(HttpStatus.NOT_FOUND, "PAYMENT_NOT_FOUND", "Payment not found",
				ex);
	}

	@ExceptionHandler(PaymentNotProcessedException.class)
	public ResponseEntity<ErrorResource> handlePaymentNotProcessed(PaymentNotProcessedException ex) {
		return ErrorResponseAssembler.buildErrorResponse(HttpStatus.CONFLICT, "PAYMENT_NOT_PROCESSED",
				"Payment is not processed", ex);
	}

	@ExceptionHandler(InvoiceAlreadyIssuedException.class)
	public ResponseEntity<ErrorResource> handleInvoiceAlreadyIssued(InvoiceAlreadyIssuedException ex) {
		return ErrorResponseAssembler.buildErrorResponse(HttpStatus.CONFLICT, "INVOICE_ALREADY_ISSUED",
				"Invoice already issued", ex);
	}

	@ExceptionHandler(ChargeServiceRequestNotFoundException.class)
	public ResponseEntity<ErrorResource> handleChargeServiceRequestNotFound(
			ChargeServiceRequestNotFoundException ex) {
		return ErrorResponseAssembler.buildErrorResponse(HttpStatus.NOT_FOUND, "SERVICE_REQUEST_NOT_FOUND",
				"Service request not found", ex);
	}

	@ExceptionHandler(BillingStayNotFoundException.class)
	public ResponseEntity<ErrorResource> handleBillingStayNotFound(BillingStayNotFoundException ex) {
		return ErrorResponseAssembler.buildErrorResponse(HttpStatus.NOT_FOUND, "STAY_NOT_FOUND", "Stay not found", ex);
	}
}
