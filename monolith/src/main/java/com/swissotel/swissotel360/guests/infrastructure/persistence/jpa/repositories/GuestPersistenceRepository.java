package com.swissotel.swissotel360.guests.infrastructure.persistence.jpa.repositories;

import com.swissotel.swissotel360.guests.infrastructure.persistence.jpa.entities.GuestPersistenceEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface GuestPersistenceRepository extends JpaRepository<GuestPersistenceEntity, Long> {
	@Override
	@EntityGraph(attributePaths = "documentType")
	Optional<GuestPersistenceEntity> findById(Long id);

	@EntityGraph(attributePaths = "documentType")
	Optional<GuestPersistenceEntity> findByPublicId(UUID publicId);

	@EntityGraph(attributePaths = "documentType")
	Optional<GuestPersistenceEntity> findByUserId(Long userId);

	@EntityGraph(attributePaths = "documentType")
	Optional<GuestPersistenceEntity> findByDocumentType_IdAndDocumentNumber(Long documentTypeId, String documentNumber);

	boolean existsByDocumentType_IdAndDocumentNumber(Long documentTypeId, String documentNumber);

	boolean existsByUserId(Long userId);

	@Override
	@EntityGraph(attributePaths = "documentType")
	Page<GuestPersistenceEntity> findAll(Pageable pageable);

	@EntityGraph(attributePaths = "documentType")
	Page<GuestPersistenceEntity> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
			String firstName, String lastName, Pageable pageable);
}
