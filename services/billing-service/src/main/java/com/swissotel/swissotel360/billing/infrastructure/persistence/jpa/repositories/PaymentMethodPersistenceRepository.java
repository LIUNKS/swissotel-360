package com.swissotel.swissotel360.billing.infrastructure.persistence.jpa.repositories;

import com.swissotel.swissotel360.billing.infrastructure.persistence.jpa.entities.PaymentMethodPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PaymentMethodPersistenceRepository extends JpaRepository<PaymentMethodPersistenceEntity, Long> {
	Optional<PaymentMethodPersistenceEntity> findByCode(String code);

	List<PaymentMethodPersistenceEntity> findAllByOrderByNameAsc();
}