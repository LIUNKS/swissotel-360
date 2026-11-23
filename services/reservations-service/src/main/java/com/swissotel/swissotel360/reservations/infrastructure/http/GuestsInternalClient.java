package com.swissotel.swissotel360.reservations.infrastructure.http;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.UUID;

@HttpExchange
public interface GuestsInternalClient {

	@GetExchange("/internal/guests/{guestId}")
	GuestCollaboratorSnapshot getById(@PathVariable("guestId") Long guestId);

	@GetExchange("/internal/guests/by-public-id/{guestPublicId}")
	GuestCollaboratorSnapshot getByPublicId(@PathVariable("guestPublicId") UUID guestPublicId);

	@GetExchange("/internal/guests/by-user-id/{userId}")
	GuestCollaboratorSnapshot getByUserId(@PathVariable("userId") Long userId);
}
