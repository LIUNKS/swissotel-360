package com.swissotel.swissotel360.guests.interfaces.acl;

import com.swissotel.swissotel360.guests.application.queryservices.GuestQueryService;
import com.swissotel.swissotel360.guests.domain.model.aggregates.Guest;
import org.springframework.stereotype.Component;
import java.util.Optional;
import java.util.UUID;

@Component
public class GuestDirectoryContextFacadeImpl implements GuestDirectoryContextFacade {
    private final GuestQueryService guests;
    public GuestDirectoryContextFacadeImpl(GuestQueryService guests) { this.guests=guests; }
    @Override public Optional<GuestDirectorySnapshot> findByPublicId(UUID id) { return guests.findByPublicId(id).map(GuestDirectoryContextFacadeImpl::snapshot); }
    @Override public Optional<GuestDirectorySnapshot> findById(Long id) { return guests.findById(id).map(GuestDirectoryContextFacadeImpl::snapshot); }
    @Override public Optional<GuestDirectorySnapshot> findByUserId(Long id) { return guests.findByUserId(id).map(GuestDirectoryContextFacadeImpl::snapshot); }
    private static GuestDirectorySnapshot snapshot(Guest g) { return new GuestDirectorySnapshot(g.getId(), g.getPublicId(), g.getUserId(), (g.getFirstName()+" "+g.getLastName()).trim(), g.getEmail()); }
}
