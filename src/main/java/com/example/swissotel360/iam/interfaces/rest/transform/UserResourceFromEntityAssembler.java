package com.example.swissotel360.iam.interfaces.rest.transform;

import com.example.swissotel360.iam.domain.model.aggregates.User;
import com.example.swissotel360.iam.interfaces.rest.resources.UserResource;

public class UserResourceFromEntityAssembler {
    private UserResourceFromEntityAssembler() {
    }

    public static UserResource toResourceFromEntity(User entity) {
        return new UserResource(entity.getId(), entity.getPublicId(), entity.getFullName(), entity.getEmail(),
                entity.getRole().getStringName());
    }
}
