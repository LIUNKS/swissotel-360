package com.swissotel.swissotel360.guests.application.internal.queryservices;

import com.swissotel.swissotel360.guests.application.queryservices.GuestQueryService;
import com.swissotel.swissotel360.guests.domain.exceptions.DocumentTypeNotFoundException;
import com.swissotel.swissotel360.guests.domain.exceptions.GuestNotFoundException;
import com.swissotel.swissotel360.guests.domain.model.aggregates.Guest;
import com.swissotel.swissotel360.guests.domain.model.queries.GuestPage;
import com.swissotel.swissotel360.guests.domain.model.valueobjects.DocumentType;
import com.swissotel.swissotel360.guests.domain.repositories.DocumentTypeRepository;
import com.swissotel.swissotel360.guests.domain.repositories.GuestRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
public class GuestQueryServiceImpl implements GuestQueryService {
	private final GuestRepository guestRepository;
	private final DocumentTypeRepository documentTypeRepository;

	public GuestQueryServiceImpl(GuestRepository guestRepository, DocumentTypeRepository documentTypeRepository) {
		this.guestRepository = guestRepository;
		this.documentTypeRepository = documentTypeRepository;
	}

	@Override
	public Guest getByPublicId(UUID publicId) {
		return guestRepository.findByPublicId(publicId)
				.orElseThrow(() -> new GuestNotFoundException(publicId.toString()));
	}

	@Override
	public java.util.Optional<Guest> findByPublicId(UUID publicId) {
		return guestRepository.findByPublicId(publicId);
	}

	@Override
	public java.util.Optional<Guest> findById(Long id) {
		return guestRepository.findById(id);
	}

	@Override
	public java.util.Optional<Guest> findByUserId(Long userId) {
		return guestRepository.findByUserId(userId);
	}

	@Override
	public Guest findByDocument(String documentTypeCode, String documentNumber) {
		var code = documentTypeCode == null ? "" : documentTypeCode.trim();
		var documentType = documentTypeRepository.findByCode(code)
				.orElseThrow(() -> new DocumentTypeNotFoundException(code));
		return guestRepository.findByDocument(documentType.id(), documentNumber.trim())
				.orElseThrow(() -> new GuestNotFoundException("document"));
	}

	@Override
	public GuestPage search(String query, int page, int size) {
		return guestRepository.search(query, page, size);
	}

	@Override
	public List<DocumentType> getDocumentTypes() {
		return documentTypeRepository.findAll();
	}
}
