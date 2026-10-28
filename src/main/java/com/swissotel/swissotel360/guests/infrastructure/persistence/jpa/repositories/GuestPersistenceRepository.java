package com.swissotel.swissotel360.guests.infrastructure.persistence.jpa.repositories;

import com.swissotel.swissotel360.guests.infrastructure.persistence.jpa.entities.GuestPersistenceEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface GuestPersistenceRepository extends JpaRepository<GuestPersistenceEntity, Long> {
    Optional<GuestPersistenceEntity> findByPublicId(UUID publicId);
    Optional<GuestPersistenceEntity> findByUserId(Long userId);
    Optional<GuestPersistenceEntity> findByDocumentType_IdAndDocumentNumber(Long documentTypeId, String documentNumber);
    boolean existsByDocumentType_IdAndDocumentNumber(Long documentTypeId, String documentNumber);
    boolean existsByUserId(Long userId);
    Page<GuestPersistenceEntity> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            String firstName, String lastName, Pageable pageable);
}
