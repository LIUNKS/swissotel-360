package com.swissotel.swissotel360.reservations.application.port;

import com.swissotel.swissotel360.reservations.domain.model.GuestId;
import com.swissotel.swissotel360.reservations.domain.model.UserId;
import java.util.Optional;
import java.util.UUID;

public interface GuestDirectoryPort {
    Optional<GuestSnapshot> findByPublicId(UUID guestPublicId);
    Optional<GuestSnapshot> findByUserId(UserId userId);
    Optional<GuestSnapshot> findById(GuestId guestId);
    record GuestSnapshot(GuestId id, UUID publicId, String fullName, String email) { }
}
