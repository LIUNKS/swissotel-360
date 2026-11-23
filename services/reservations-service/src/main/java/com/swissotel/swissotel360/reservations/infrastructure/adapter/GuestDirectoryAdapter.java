package com.swissotel.swissotel360.reservations.infrastructure.adapter;

import com.swissotel.swissotel360.reservations.application.port.GuestDirectoryPort;
import com.swissotel.swissotel360.reservations.domain.model.GuestId;
import com.swissotel.swissotel360.reservations.domain.model.UserId;
import com.swissotel.swissotel360.reservations.infrastructure.http.GuestCollaboratorSnapshot;
import com.swissotel.swissotel360.reservations.infrastructure.http.GuestsInternalClient;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

@Component
public class GuestDirectoryAdapter implements GuestDirectoryPort {
	private final GuestsInternalClient guests;

	public GuestDirectoryAdapter(GuestsInternalClient guests) {
		this.guests = guests;
	}

	@Override
	public Optional<GuestSnapshot> findByPublicId(UUID id) {
		return getOrEmpty(() -> guests.getByPublicId(id)).map(GuestDirectoryAdapter::map);
	}

	@Override
	public Optional<GuestSnapshot> findByUserId(UserId id) {
		return getOrEmpty(() -> guests.getByUserId(id.value())).map(GuestDirectoryAdapter::map);
	}

	@Override
	public Optional<GuestSnapshot> findById(GuestId id) {
		return getOrEmpty(() -> guests.getById(id.value())).map(GuestDirectoryAdapter::map);
	}

	private static Optional<GuestCollaboratorSnapshot> getOrEmpty(Supplier<GuestCollaboratorSnapshot> call) {
		try {
			return Optional.ofNullable(call.get());
		} catch (HttpClientErrorException.NotFound ignored) {
			return Optional.empty();
		}
	}

	private static GuestSnapshot map(GuestCollaboratorSnapshot g) {
		return new GuestSnapshot(new GuestId(g.id()), g.publicId(), g.fullName(), g.email());
	}
}
