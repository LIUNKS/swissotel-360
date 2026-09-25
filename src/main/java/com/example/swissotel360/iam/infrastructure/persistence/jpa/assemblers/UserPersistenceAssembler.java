package com.example.swissotel360.iam.infrastructure.persistence.jpa.assemblers;

import com.example.swissotel360.iam.domain.model.aggregates.User;
import com.example.swissotel360.iam.domain.model.valueobjects.EmailAddress;
import com.example.swissotel360.iam.infrastructure.persistence.jpa.entities.UserPersistenceEntity;

public class UserPersistenceAssembler {
    public static UserPersistenceEntity toPersistenceEntity(User user) {
        if (user == null)
            return null;
        var entity = new UserPersistenceEntity();
        if (user.getId() != null)
            entity.setId(user.getId());
        entity.setPublicId(user.getPublicId());
        entity.setFullName(user.getFullName());
        entity.setEmail(user.getEmail());
        entity.setPassword(user.getPassword());
        entity.setStatus(user.getStatus());
        entity.setLastLoginAt(user.getLastLoginAt());
        entity.setRole(RolePersistenceAssembler.toPersistenceEntity(user.getRole()));
        return entity;
    }

    public static User toDomainEntity(UserPersistenceEntity entity) {
        if (entity == null)
            return null;
        return new User(
                entity.getId(),
                entity.getPublicId(),
                entity.getFullName(),
                new EmailAddress(entity.getEmail()),
                entity.getPassword(),
                RolePersistenceAssembler.toDomainEntity(entity.getRole()),
                entity.getStatus(),
                entity.getLastLoginAt());
    }
}
