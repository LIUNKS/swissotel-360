package com.swissotel.swissotel360.servicerequests.infrastructure.persistence.jpa.repositories;

import com.swissotel.swissotel360.servicerequests.infrastructure.persistence.jpa.entities.ServiceRequestPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ServiceRequestPersistenceRepository extends JpaRepository<ServiceRequestPersistenceEntity, Long> {
	Optional<ServiceRequestPersistenceEntity> findByPublicId(UUID publicId);

	List<ServiceRequestPersistenceEntity> findAllByOrderByRequestedAtDesc();

	List<ServiceRequestPersistenceEntity> findByStayIdOrderByRequestedAtDesc(Long stayId);

	List<ServiceRequestPersistenceEntity> findByStatusOrderByRequestedAtDesc(String status);

	List<ServiceRequestPersistenceEntity> findByStayIdAndStatusOrderByRequestedAtDesc(Long stayId, String status);
}
