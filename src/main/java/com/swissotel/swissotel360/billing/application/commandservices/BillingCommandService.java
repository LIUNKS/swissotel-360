package com.swissotel.swissotel360.billing.application.commandservices;

import com.swissotel.swissotel360.billing.application.command.*;
import com.swissotel.swissotel360.billing.domain.aggregates.Invoice;
import com.swissotel.swissotel360.billing.domain.aggregates.Payment;
import com.swissotel.swissotel360.billing.domain.model.Charge;

public interface BillingCommandService {
	/** Idempotent: does nothing when the stay already has a folio. */
	void registerFolio(RegisterFolioCommand command);

	Charge addCharge(AddChargeCommand command);

	/** Idempotent; skips silently when the stay has no open folio. */
	void recordServiceRequestCharge(RecordServiceRequestChargeCommand command);

	Payment processPayment(ProcessPaymentCommand command);

	Invoice issueInvoice(IssueInvoiceCommand command);
}