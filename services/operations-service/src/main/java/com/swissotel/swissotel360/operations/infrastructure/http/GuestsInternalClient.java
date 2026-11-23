package com.swissotel.swissotel360.operations.infrastructure.http;

import com.swissotel.swissotel360.stays.application.internal.outboundservices.acl.StayGuestSnapshot;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange
public interface GuestsInternalClient {

	@GetExchange("/internal/guests/{guestId}")
	StayGuestSnapshot getById(@PathVariable("guestId") Long guestId);

	@GetExchange("/internal/guests/by-user-id/{userId}")
	StayGuestSnapshot getByUserId(@PathVariable("userId") Long userId);
}
