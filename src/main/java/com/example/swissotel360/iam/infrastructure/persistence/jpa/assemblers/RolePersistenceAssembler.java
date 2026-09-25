package com.example.swissotel360.iam.infrastructure.persistence.jpa.assemblers;

import com.example.swissotel360.iam.domain.model.entities.Role;
import com.example.swissotel360.iam.infrastructure.persistence.jpa.entities.RolePersistenceEntity;
import com.example.swissotel360.iam.domain.model.valueobjects.Roles;

public class RolePersistenceAssembler {
    public static RolePersistenceEntity toPersistenceEntity(Role role) {
        if (role == null)
            return null;
        var entity = new RolePersistenceEntity();
        if (role.getId() != null)
            entity.setId(role.getId());
        entity.setName(role.getName());
        return entity;
    }

    public static Role toDomainEntity(RolePersistenceEntity entity) {
        if (entity == null)
            return null;
        var role = new Role(Roles.valueOf(entity.getName().name()));
        role.setId(entity.getId());
        return role;
    }
}
