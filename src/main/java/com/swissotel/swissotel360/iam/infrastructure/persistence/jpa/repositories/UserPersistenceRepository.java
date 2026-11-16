package com.swissotel.swissotel360.iam.infrastructure.persistence.jpa.repositories;

import com.swissotel.swissotel360.iam.infrastructure.persistence.jpa.entities.UserPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface UserPersistenceRepository extends JpaRepository<UserPersistenceEntity, Long> {
	Optional<UserPersistenceEntity> findByEmail(String email);

	Optional<UserPersistenceEntity> findByPublicId(UUID publicId);

	boolean existsByEmail(String email);
}
