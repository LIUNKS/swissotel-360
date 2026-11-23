package com.swissotel.swissotel360.guests.infrastructure.http;

import com.swissotel.swissotel360.guests.application.internal.outboundservices.acl.IamGuestAccountOutboundService;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Optional;
import java.util.UUID;

@Component
public class IamHttpGuestAccountAdapter implements IamGuestAccountOutboundService {

	private final IamInternalClient client;

	public IamHttpGuestAccountAdapter(IamInternalClient client) {
		this.client = client;
	}

	@Override
	public Optional<Long> fetchInternalUserId(UUID userPublicId) {
		try {
			return Optional.ofNullable(client.getUserByPublicId(userPublicId)).map(IamUserSnapshot::id);
		} catch (HttpClientErrorException.NotFound ignored) {
			return Optional.empty();
		}
	}
}
