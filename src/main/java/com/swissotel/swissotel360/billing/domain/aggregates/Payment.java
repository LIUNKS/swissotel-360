package com.swissotel.swissotel360.billing.domain.aggregates;

import com.swissotel.swissotel360.billing.domain.model.Money;
import com.swissotel.swissotel360.billing.domain.model.PaymentId;
import com.swissotel.swissotel360.billing.domain.model.PaymentMethod;
import com.swissotel.swissotel360.billing.domain.model.PaymentStatus;
import com.swissotel.swissotel360.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import java.math.BigDecimal;
import java.time.Instant;

public final class Payment extends AbstractDomainAggregateRoot<Payment> {
	private final PaymentId id;
	private final Long dbId;
	private final Long folioId;
	private final PaymentMethod method;
	private final BigDecimal amount;
	private final PaymentStatus status;
	private final String gatewayReference;
	private final Instant paidAt;

	private Payment(PaymentId id, Long dbId, Long folioId, PaymentMethod method, BigDecimal amount,
			PaymentStatus status, String gatewayReference, Instant paidAt) {
		if (id == null || folioId == null || method == null || status == null || paidAt == null)
			throw new IllegalArgumentException("Payment fields are required");
		this.id = id;
		this.dbId = dbId;
		this.folioId = folioId;
		this.method = method;
		this.amount = Money.of(amount);
		this.status = status;
		this.gatewayReference = gatewayReference;
		this.paidAt = paidAt;
	}

	/** The (simulated) gateway approved the payment. */
	public static Payment processed(Long folioId, PaymentMethod method, BigDecimal amount, String gatewayReference,
			Instant now) {
		return new Payment(PaymentId.newId(), null, folioId, method, amount, PaymentStatus.PROCESSED,
				gatewayReference, now);
	}

	public static Payment restore(PaymentId id, Long dbId, Long folioId, PaymentMethod method, BigDecimal amount,
			PaymentStatus status, String gatewayReference, Instant paidAt) {
		return new Payment(id, dbId, folioId, method, amount, status, gatewayReference, paidAt);
	}

	public boolean isProcessed() {
		return status == PaymentStatus.PROCESSED;
	}

	public PaymentId id() {
		return id;
	}

	public Long dbId() {
		return dbId;
	}

	public Long folioId() {
		return folioId;
	}

	public PaymentMethod method() {
		return method;
	}

	public BigDecimal amount() {
		return amount;
	}

	public PaymentStatus status() {
		return status;
	}

	public String gatewayReference() {
		return gatewayReference;
	}

	public Instant paidAt() {
		return paidAt;
	}
}
