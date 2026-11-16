package com.swissotel.swissotel360.billing.infrastructure.transaction;

import com.swissotel.swissotel360.billing.application.command.*;
import com.swissotel.swissotel360.billing.application.commandservices.BillingCommandService;
import com.swissotel.swissotel360.billing.domain.aggregates.Invoice;
import com.swissotel.swissotel360.billing.domain.aggregates.Payment;
import com.swissotel.swissotel360.billing.domain.model.Charge;
import org.springframework.transaction.support.TransactionTemplate;

/** Infrastructure decorator keeps transaction mechanics out of the application layer. */
public final class TransactionalBillingCommandService implements BillingCommandService {
	private final BillingCommandService delegate;
	private final TransactionTemplate transactions;

	public TransactionalBillingCommandService(BillingCommandService delegate, TransactionTemplate transactions) {
		this.delegate = delegate;
		this.transactions = transactions;
	}

	@Override
	public void registerFolio(RegisterFolioCommand c) {
		transactions.executeWithoutResult(s -> delegate.registerFolio(c));
	}

	@Override
	public Charge addCharge(AddChargeCommand c) {
		return transactions.execute(s -> delegate.addCharge(c));
	}

	@Override
	public void recordServiceRequestCharge(RecordServiceRequestChargeCommand c) {
		transactions.executeWithoutResult(s -> delegate.recordServiceRequestCharge(c));
	}

	@Override
	public Payment processPayment(ProcessPaymentCommand c) {
		return transactions.execute(s -> delegate.processPayment(c));
	}

	@Override
	public Invoice issueInvoice(IssueInvoiceCommand c) {
		return transactions.execute(s -> delegate.issueInvoice(c));
	}
}