package com.example.swissotel360.iam.domain.repositories;

import com.example.swissotel360.iam.domain.model.aggregates.User;
import java.util.Optional;

/**
 * Domain repository port for IAM User aggregate.
 */
public interface UserRepository {
    Optional<User> findByEmail(String email);

    Optional<User> findById(Long id);

    User save(User user);

    boolean existsByEmail(String email);
}
