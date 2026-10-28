package com.swissotel.swissotel360.billing.domain.aggregates;

import com.swissotel.swissotel360.billing.domain.exceptions.PaymentNotProcessedException;
import com.swissotel.swissotel360.billing.domain.model.DocumentType;
import com.swissotel.swissotel360.billing.domain.model.InvoiceId;
import com.swissotel.swissotel360.billing.domain.model.Money;
import com.swissotel.swissotel360.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import java.math.BigDecimal;
import java.time.Instant;

public final class Invoice extends AbstractDomainAggregateRoot<Invoice> {
	private final InvoiceId id;
	private final Long dbId;
	private final Long paymentId;
	private final DocumentType documentType;
	private final String series;
	private final String number;
	private final Instant issuedAt;
	private final BigDecimal totalAmount;

	private Invoice(InvoiceId id, Long dbId, Long paymentId, DocumentType documentType, String series, String number,
			Instant issuedAt, BigDecimal totalAmount) {
		if (id == null || paymentId == null || documentType == null || series == null || number == null
				|| issuedAt == null)
			throw new IllegalArgumentException("Invoice fields are required");
		this.id = id;
		this.dbId = dbId;
		this.paymentId = paymentId;
		this.documentType = documentType;
		this.series = series;
		this.number = number;
		this.issuedAt = issuedAt;
		this.totalAmount = Money.of(totalAmount);
	}

	/** Only processed payments can be invoiced; the series is derived from the document type. */
	public static Invoice issue(Payment payment, DocumentType type, String number, Instant now) {
		if (!payment.isProcessed())
			throw new PaymentNotProcessedException();
		return new Invoice(InvoiceId.newId(), null, payment.dbId(), type, type.series(), number, now,
				payment.amount());
	}

	public static Invoice restore(InvoiceId id, Long dbId, Long paymentId, DocumentType documentType, String series,
			String number, Instant issuedAt, BigDecimal totalAmount) {
		return new Invoice(id, dbId, paymentId, documentType, series, number, issuedAt, totalAmount);
	}

	public InvoiceId id() {
		return id;
	}

	public Long dbId() {
		return dbId;
	}

	public Long paymentId() {
		return paymentId;
	}

	public DocumentType documentType() {
		return documentType;
	}

	public String series() {
		return series;
	}

	public String number() {
		return number;
	}

	public Instant issuedAt() {
		return issuedAt;
	}

	public BigDecimal totalAmount() {
		return totalAmount;
	}
}
