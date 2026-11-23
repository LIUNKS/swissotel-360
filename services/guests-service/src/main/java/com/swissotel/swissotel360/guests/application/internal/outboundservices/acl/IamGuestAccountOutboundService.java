package com.swissotel.swissotel360.guests.application.internal.outboundservices.acl;

import java.util.Optional;
import java.util.UUID;

/** Outbound port used by guests to resolve IAM user accounts without sharing persistence. */
public interface IamGuestAccountOutboundService {
	Optional<Long> fetchInternalUserId(UUID userPublicId);
}
