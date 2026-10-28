package com.swissotel.swissotel360.iam.infrastructure.persistence.jpa.adapters;

import com.swissotel.swissotel360.iam.domain.model.aggregates.User;
import com.swissotel.swissotel360.iam.domain.repositories.UserRepository;
import com.swissotel.swissotel360.iam.infrastructure.persistence.jpa.assemblers.UserPersistenceAssembler;
import com.swissotel.swissotel360.iam.infrastructure.persistence.jpa.entities.UserPersistenceEntity;
import com.swissotel.swissotel360.iam.infrastructure.persistence.jpa.repositories.UserPersistenceRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class UserRepositoryImpl implements UserRepository {
    private final UserPersistenceRepository repository;

    public UserRepositoryImpl(UserPersistenceRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return repository.findByEmail(email).map(UserPersistenceAssembler::toDomainEntity);
    }

    @Override
    public Optional<User> findById(Long id) {
        return repository.findById(id).map(UserPersistenceAssembler::toDomainEntity);
    }

    @Override
    public Optional<User> findByPublicId(UUID publicId) {
        return repository.findByPublicId(publicId).map(UserPersistenceAssembler::toDomainEntity);
    }

    @Override
    public User save(User user) {
        UserPersistenceEntity entityToSave;
        if (user.getId() != null) {
            var existingEntityOpt = repository.findById(user.getId());
            if (existingEntityOpt.isPresent()) {
                var existingEntity = existingEntityOpt.get();
                existingEntity.setPublicId(user.getPublicId());
                existingEntity.setFullName(user.getFullName());
                existingEntity.setEmail(user.getEmail());
                existingEntity.setPassword(user.getPassword());
                existingEntity.setStatus(user.getStatus());
                existingEntity.setLastLoginAt(user.getLastLoginAt());
                entityToSave = existingEntity;
            } else {
                entityToSave = UserPersistenceAssembler.toPersistenceEntity(user);
            }
        } else {
            entityToSave = UserPersistenceAssembler.toPersistenceEntity(user);
        }
        var savedEntity = repository.save(entityToSave);
        return UserPersistenceAssembler.toDomainEntity(savedEntity);
    }

    @Override
    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }
}
