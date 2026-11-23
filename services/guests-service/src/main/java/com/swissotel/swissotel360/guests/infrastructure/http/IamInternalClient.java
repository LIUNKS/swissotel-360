package com.swissotel.swissotel360.guests.infrastructure.http;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.UUID;

@HttpExchange
public interface IamInternalClient {

	@GetExchange("/internal/users/by-public-id/{userPublicId}")
	IamUserSnapshot getUserByPublicId(@PathVariable("userPublicId") UUID userPublicId);
}
