package com.swissotel.swissotel360.billing.infrastructure.persistence.jpa.repositories;

import com.swissotel.swissotel360.billing.infrastructure.persistence.jpa.entities.FolioPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface FolioPersistenceRepository extends JpaRepository<FolioPersistenceEntity, Long> {
	Optional<FolioPersistenceEntity> findByPublicId(UUID publicId);

	Optional<FolioPersistenceEntity> findByStayId(Long stayId);
}