package com.swissotel.swissotel360.billing.domain.aggregates;

import com.swissotel.swissotel360.billing.domain.exceptions.FolioClosedException;
import com.swissotel.swissotel360.billing.domain.exceptions.PaymentAmountMismatchException;
import com.swissotel.swissotel360.billing.domain.model.Charge;
import com.swissotel.swissotel360.billing.domain.model.FolioId;
import com.swissotel.swissotel360.billing.domain.model.FolioStatus;
import com.swissotel.swissotel360.billing.domain.model.Money;
import com.swissotel.swissotel360.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public final class Folio extends AbstractDomainAggregateRoot<Folio> {
	private final FolioId id;
	private final Long dbId;
	private final Long stayId;
	private final List<Charge> charges;
	private FolioStatus status;

	private Folio(FolioId id, Long dbId, Long stayId, FolioStatus status, List<Charge> charges) {
		if (id == null || stayId == null || status == null)
			throw new IllegalArgumentException("Folio fields are required");
		this.id = id;
		this.dbId = dbId;
		this.stayId = stayId;
		this.status = status;
		this.charges = new ArrayList<>(charges);
	}

	public static Folio open(Long stayId) {
		return new Folio(FolioId.newId(), null, stayId, FolioStatus.OPEN, List.of());
	}

	public static Folio restore(FolioId id, Long dbId, Long stayId, FolioStatus status, List<Charge> charges) {
		return new Folio(id, dbId, stayId, status, charges);
	}

	public Charge addCharge(String concept, BigDecimal amount, Long serviceRequestId, Instant now) {
		if (status != FolioStatus.OPEN)
			throw new FolioClosedException();
		var charge = new Charge(null, serviceRequestId, concept, amount, now);
		charges.add(charge);
		return charge;
	}

	/** Validates a full-balance payment and closes the folio. */
	public void pay(BigDecimal amount) {
		if (status != FolioStatus.OPEN)
			throw new FolioClosedException();
		var total = total();
		if (total.signum() == 0 || Money.of(amount).compareTo(total) != 0)
			throw new PaymentAmountMismatchException();
		status = FolioStatus.CLOSED;
	}

	public BigDecimal total() {
		return charges.stream().map(Charge::amount).reduce(Money.of(BigDecimal.ZERO), BigDecimal::add);
	}

	public boolean isOpen() {
		return status == FolioStatus.OPEN;
	}

	public FolioId id() {
		return id;
	}

	public Long dbId() {
		return dbId;
	}

	public Long stayId() {
		return stayId;
	}

	public FolioStatus status() {
		return status;
	}

	public List<Charge> charges() {
		return List.copyOf(charges);
	}
}
