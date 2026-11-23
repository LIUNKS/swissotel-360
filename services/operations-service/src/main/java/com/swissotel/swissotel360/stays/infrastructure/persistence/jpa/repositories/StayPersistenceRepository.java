package com.swissotel.swissotel360.stays.infrastructure.persistence.jpa.repositories;

import com.swissotel.swissotel360.stays.infrastructure.persistence.jpa.entities.StayPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StayPersistenceRepository extends JpaRepository<StayPersistenceEntity, Long> {
	Optional<StayPersistenceEntity> findByPublicId(UUID publicId);

	boolean existsByReservationId(Long reservationId);

	List<StayPersistenceEntity> findAllByOrderByCheckedInAtDesc();

	List<StayPersistenceEntity> findByStatusOrderByCheckedInAtDesc(String status);
}
