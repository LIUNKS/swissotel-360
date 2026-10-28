package com.swissotel.swissotel360.reservations.infrastructure.adapter;

import com.swissotel.swissotel360.guests.interfaces.acl.GuestDirectoryContextFacade;
import com.swissotel.swissotel360.reservations.application.port.GuestDirectoryPort;
import com.swissotel.swissotel360.reservations.domain.model.GuestId;
import com.swissotel.swissotel360.reservations.domain.model.UserId;
import org.springframework.stereotype.Component;
import java.util.Optional;
import java.util.UUID;

@Component
public class GuestDirectoryAdapter implements GuestDirectoryPort {
    private final GuestDirectoryContextFacade guests;
    public GuestDirectoryAdapter(GuestDirectoryContextFacade guests) { this.guests=guests; }
    @Override public Optional<GuestSnapshot> findByPublicId(UUID id) { return guests.findByPublicId(id).map(GuestDirectoryAdapter::map); }
    @Override public Optional<GuestSnapshot> findByUserId(UserId id) { return guests.findByUserId(id.value()).map(GuestDirectoryAdapter::map); }
    @Override public Optional<GuestSnapshot> findById(GuestId id) { return guests.findById(id.value()).map(GuestDirectoryAdapter::map); }
    private static GuestSnapshot map(com.swissotel.swissotel360.guests.interfaces.acl.GuestDirectorySnapshot g) {
        return new GuestSnapshot(new GuestId(g.id()),g.publicId(),g.fullName(),g.email());
    }
}
