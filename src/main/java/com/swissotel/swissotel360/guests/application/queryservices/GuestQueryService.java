package com.swissotel.swissotel360.guests.application.queryservices;

import com.swissotel.swissotel360.guests.domain.model.aggregates.Guest;
import com.swissotel.swissotel360.guests.domain.model.queries.GuestPage;
import com.swissotel.swissotel360.guests.domain.model.valueobjects.DocumentType;
import java.util.List;
import java.util.UUID;

public interface GuestQueryService {
	Guest getByPublicId(UUID publicId);

	java.util.Optional<Guest> findByPublicId(UUID publicId);

	java.util.Optional<Guest> findById(Long id);

	java.util.Optional<Guest> findByUserId(Long userId);

	Guest findByDocument(Long documentTypeId, String documentNumber);

	GuestPage search(String query, int page, int size);

	List<DocumentType> getDocumentTypes();
}
