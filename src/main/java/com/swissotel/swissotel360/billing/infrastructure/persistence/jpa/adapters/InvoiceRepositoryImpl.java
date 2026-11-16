package com.swissotel.swissotel360.billing.infrastructure.persistence.jpa.adapters;

import com.swissotel.swissotel360.billing.domain.aggregates.Invoice;
import com.swissotel.swissotel360.billing.domain.repositories.InvoiceRepository;
import com.swissotel.swissotel360.billing.infrastructure.persistence.jpa.assemblers.BillingPersistenceAssembler;
import com.swissotel.swissotel360.billing.infrastructure.persistence.jpa.entities.InvoicePersistenceEntity;
import com.swissotel.swissotel360.billing.infrastructure.persistence.jpa.repositories.InvoicePersistenceRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public class InvoiceRepositoryImpl implements InvoiceRepository {
	private final InvoicePersistenceRepository invoices;

	public InvoiceRepositoryImpl(InvoicePersistenceRepository invoices) {
		this.invoices = invoices;
	}

	@Override
	public Invoice save(Invoice invoice) {
		var entity = invoice.dbId() == null ? new InvoicePersistenceEntity()
				: invoices.findById(invoice.dbId()).orElseThrow();
		return BillingPersistenceAssembler
				.toDomain(invoices.saveAndFlush(BillingPersistenceAssembler.toEntity(invoice, entity)));
	}

	@Override
	public boolean existsByPaymentId(Long paymentId) {
		return invoices.existsByPaymentId(paymentId);
	}

	@Override
	public Optional<String> findLastNumber(String series) {
		return invoices.findLastNumber(series);
	}
}