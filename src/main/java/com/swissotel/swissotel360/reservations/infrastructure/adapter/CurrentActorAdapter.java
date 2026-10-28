package com.swissotel.swissotel360.reservations.infrastructure.adapter;

import com.swissotel.swissotel360.iam.interfaces.acl.IamContextFacade;
import com.swissotel.swissotel360.reservations.application.port.CurrentActorPort;
import com.swissotel.swissotel360.reservations.domain.model.UserId;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import java.util.stream.Collectors;

@Component
public class CurrentActorAdapter implements CurrentActorPort {
    private final IamContextFacade iam;
    public CurrentActorAdapter(IamContextFacade iam) { this.iam=iam; }
    @Override public CurrentActor current() {
        var authentication=SecurityContextHolder.getContext().getAuthentication();
        if (authentication==null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof UserDetails principal))
            throw new IllegalStateException("An authenticated user is required");
        var userId=iam.fetchUserIdByEmail(principal.getUsername());
        if (userId==null || userId<1) throw new IllegalStateException("Authenticated IAM user could not be resolved");
        var authorities=authentication.getAuthorities().stream().map(a->a.getAuthority()).collect(Collectors.toSet());
        var role=authorities.stream().filter(a->a.startsWith("ROLE_")).findFirst().orElse("");
        var permissions=authorities.stream().filter(a->!a.startsWith("ROLE_")).collect(Collectors.toSet());
        return new CurrentActor(new UserId(userId),role,permissions);
    }
}
