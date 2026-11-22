package com.swissotel.swissotel360.billing.infrastructure.persistence.jpa.repositories;

import com.swissotel.swissotel360.billing.infrastructure.persistence.jpa.entities.ChargePersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChargePersistenceRepository extends JpaRepository<ChargePersistenceEntity, Long> {
	List<ChargePersistenceEntity> findByFolioIdOrderByIdAsc(Long folioId);

	boolean existsByServiceRequestId(Long serviceRequestId);
}