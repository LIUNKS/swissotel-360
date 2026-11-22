package com.swissotel.swissotel360.billing.infrastructure.persistence.jpa.adapters;

import com.swissotel.swissotel360.billing.domain.aggregates.Payment;
import com.swissotel.swissotel360.billing.domain.model.PaymentId;
import com.swissotel.swissotel360.billing.domain.model.PaymentMethod;
import com.swissotel.swissotel360.billing.domain.repositories.PaymentRepository;
import com.swissotel.swissotel360.billing.infrastructure.persistence.jpa.assemblers.BillingPersistenceAssembler;
import com.swissotel.swissotel360.billing.infrastructure.persistence.jpa.entities.PaymentPersistenceEntity;
import com.swissotel.swissotel360.billing.infrastructure.persistence.jpa.repositories.PaymentMethodPersistenceRepository;
import com.swissotel.swissotel360.billing.infrastructure.persistence.jpa.repositories.PaymentPersistenceRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public class PaymentRepositoryImpl implements PaymentRepository {
	private final PaymentPersistenceRepository payments;
	private final PaymentMethodPersistenceRepository methods;

	public PaymentRepositoryImpl(PaymentPersistenceRepository payments, PaymentMethodPersistenceRepository methods) {
		this.payments = payments;
		this.methods = methods;
	}

	@Override
	public Payment save(Payment payment) {
		var entity = payment.dbId() == null ? new PaymentPersistenceEntity()
				: payments.findById(payment.dbId()).orElseThrow();
		return toDomain(payments.saveAndFlush(BillingPersistenceAssembler.toEntity(payment, entity)));
	}

	@Override
	public Optional<Payment> findByPublicId(PaymentId id) {
		return payments.findByPublicId(id.value()).map(this::toDomain);
	}

	@Override
	public boolean existsByFolioId(Long folioId) {
		return payments.existsByFolioId(folioId);
	}

	@Override
	public Optional<PaymentMethod> findMethodByCode(String code) {
		return methods.findByCode(code).map(BillingPersistenceAssembler::toDomain);
	}

	@Override
	public List<PaymentMethod> findAllMethods() {
		return methods.findAllByOrderByNameAsc().stream().map(BillingPersistenceAssembler::toDomain).toList();
	}

	private Payment toDomain(PaymentPersistenceEntity e) {
		return BillingPersistenceAssembler.toDomain(e,
				BillingPersistenceAssembler.toDomain(methods.findById(e.getPaymentMethodId()).orElseThrow()));
	}
}