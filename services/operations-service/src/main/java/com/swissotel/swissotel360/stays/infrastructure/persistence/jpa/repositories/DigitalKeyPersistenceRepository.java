package com.swissotel.swissotel360.stays.infrastructure.persistence.jpa.repositories;

import com.swissotel.swissotel360.stays.infrastructure.persistence.jpa.entities.DigitalKeyPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DigitalKeyPersistenceRepository extends JpaRepository<DigitalKeyPersistenceEntity, Long> {
	List<DigitalKeyPersistenceEntity> findByStayIdOrderByIdAsc(Long stayId);

	Optional<DigitalKeyPersistenceEntity> findByPublicId(UUID publicId);
}
