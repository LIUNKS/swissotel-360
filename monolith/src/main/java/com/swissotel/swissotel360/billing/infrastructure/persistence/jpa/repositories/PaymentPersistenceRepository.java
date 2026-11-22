package com.swissotel.swissotel360.billing.infrastructure.persistence.jpa.repositories;

import com.swissotel.swissotel360.billing.infrastructure.persistence.jpa.entities.PaymentPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface PaymentPersistenceRepository extends JpaRepository<PaymentPersistenceEntity, Long> {
	Optional<PaymentPersistenceEntity> findByPublicId(UUID publicId);

	boolean existsByFolioId(Long folioId);
}