package com.example.swissotel360.iam.interfaces.rest.transform;

import com.example.swissotel360.iam.domain.model.aggregates.User;
import com.example.swissotel360.iam.interfaces.rest.resources.AuthenticatedUserResource;

/**
 * Assembler that translates IAM authentication results into {@link AuthenticatedUserResource}.
 */
public class AuthenticatedUserResourceFromEntityAssembler {
    /**
     * Creates a resource from the authenticated {@link User} aggregate and issued bearer token.
     *
     * @param user authenticated user aggregate
     * @return resource used by the authentication endpoint response
     */
    public static AuthenticatedUserResource toResourceFromEntity(User user) {
        return new AuthenticatedUserResource(user.getId(), user.getUsername());
    }
}
