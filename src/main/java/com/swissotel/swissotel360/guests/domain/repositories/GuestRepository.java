package com.swissotel.swissotel360.guests.domain.repositories;

import com.swissotel.swissotel360.guests.domain.model.aggregates.Guest;
import com.swissotel.swissotel360.guests.domain.model.queries.GuestPage;
import java.util.Optional;
import java.util.UUID;

public interface GuestRepository {
    Guest save(Guest guest);
    Optional<Guest> findByPublicId(UUID publicId);
    Optional<Guest> findByDocument(Long documentTypeId, String documentNumber);
    boolean existsByDocument(Long documentTypeId, String documentNumber);
    GuestPage search(String query, int page, int size);
}
