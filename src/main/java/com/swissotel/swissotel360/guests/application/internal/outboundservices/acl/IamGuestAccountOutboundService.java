package com.swissotel.swissotel360.guests.application.internal.outboundservices.acl;

import com.swissotel.swissotel360.iam.interfaces.acl.IamContextFacade;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.UUID;

/** ACL adapter used by guests to resolve IAM users without accessing IAM persistence. */
@Service
public class IamGuestAccountOutboundService {
    private final IamContextFacade iamContextFacade;

    public IamGuestAccountOutboundService(IamContextFacade iamContextFacade) {
        this.iamContextFacade = iamContextFacade;
    }

    public Optional<Long> fetchInternalUserId(UUID userPublicId) {
        return iamContextFacade.fetchUserIdByPublicId(userPublicId);
    }
}
