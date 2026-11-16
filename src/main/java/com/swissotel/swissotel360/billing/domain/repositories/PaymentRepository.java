package com.swissotel.swissotel360.billing.domain.repositories;

import com.swissotel.swissotel360.billing.domain.aggregates.Payment;
import com.swissotel.swissotel360.billing.domain.model.PaymentId;
import com.swissotel.swissotel360.billing.domain.model.PaymentMethod;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository {
	Payment save(Payment payment);

	Optional<Payment> findByPublicId(PaymentId id);

	boolean existsByFolioId(Long folioId);

	Optional<PaymentMethod> findMethodByCode(String code);

	List<PaymentMethod> findAllMethods();
}
