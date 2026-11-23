package com.swissotel.swissotel360.servicerequests.infrastructure.persistence.jpa.repositories;

import com.swissotel.swissotel360.servicerequests.infrastructure.persistence.jpa.entities.ServiceTypePersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ServiceTypePersistenceRepository extends JpaRepository<ServiceTypePersistenceEntity, Long> {
	Optional<ServiceTypePersistenceEntity> findByCode(String code);

	List<ServiceTypePersistenceEntity> findAllByOrderByNameAsc();
}
