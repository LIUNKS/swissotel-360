package com.swissotel.swissotel360.iam.domain.repositories;

import com.swissotel.swissotel360.iam.domain.model.aggregates.User;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain repository port for IAM User aggregate.
 */
public interface UserRepository {
	Optional<User> findByEmail(String email);

	Optional<User> findById(Long id);

	Optional<User> findByPublicId(UUID publicId);

	User save(User user);

	boolean existsByEmail(String email);
}
