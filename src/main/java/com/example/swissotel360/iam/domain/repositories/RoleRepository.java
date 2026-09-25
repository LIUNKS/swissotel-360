package com.example.swissotel360.iam.domain.repositories;

import com.example.swissotel360.iam.domain.model.entities.Role;
import com.example.swissotel360.iam.domain.model.valueobjects.Roles;

import java.util.List;
import java.util.Optional;

/**
 * IAM role repository port.
 */
public interface RoleRepository {
    Optional<Role> findByName(Roles name);

    List<Role> findAll();

    Role save(Role role);

    boolean existsByName(Roles name);
}
