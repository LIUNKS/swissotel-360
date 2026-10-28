package com.swissotel.swissotel360.guests.infrastructure.persistence.jpa.adapters;

import com.swissotel.swissotel360.guests.domain.model.aggregates.Guest;
import com.swissotel.swissotel360.guests.domain.model.queries.GuestPage;
import com.swissotel.swissotel360.guests.domain.repositories.GuestRepository;
import com.swissotel.swissotel360.guests.infrastructure.persistence.jpa.assemblers.GuestPersistenceAssembler;
import com.swissotel.swissotel360.guests.infrastructure.persistence.jpa.repositories.DocumentTypePersistenceRepository;
import com.swissotel.swissotel360.guests.infrastructure.persistence.jpa.repositories.GuestPersistenceRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public class GuestRepositoryImpl implements GuestRepository {
	private final GuestPersistenceRepository guests;
	private final DocumentTypePersistenceRepository documentTypes;

	public GuestRepositoryImpl(GuestPersistenceRepository guests, DocumentTypePersistenceRepository documentTypes) {
		this.guests = guests;
		this.documentTypes = documentTypes;
	}

	@Override
	public Guest save(Guest guest) {
		var entity = guest.getId() == null
				? new com.swissotel.swissotel360.guests.infrastructure.persistence.jpa.entities.GuestPersistenceEntity()
				: guests.findById(guest.getId()).orElseThrow();
		var documentType = documentTypes.findById(guest.getDocumentTypeId()).orElseThrow();
		return GuestPersistenceAssembler
				.toDomain(guests.save(GuestPersistenceAssembler.toPersistence(guest, entity, documentType)));
	}

	@Override
	public Optional<Guest> findByPublicId(UUID publicId) {
		return guests.findByPublicId(publicId).map(GuestPersistenceAssembler::toDomain);
	}

	@Override
	public Optional<Guest> findById(Long id) {
		return guests.findById(id).map(GuestPersistenceAssembler::toDomain);
	}

	@Override
	public Optional<Guest> findByUserId(Long userId) {
		return guests.findByUserId(userId).map(GuestPersistenceAssembler::toDomain);
	}

	@Override
	public Optional<Guest> findByDocument(Long typeId, String number) {
		return guests.findByDocumentType_IdAndDocumentNumber(typeId, number).map(GuestPersistenceAssembler::toDomain);
	}

	@Override
	public boolean existsByDocument(Long typeId, String number) {
		return guests.existsByDocumentType_IdAndDocumentNumber(typeId, number);
	}

	@Override
	public boolean existsByUserId(Long userId) {
		return guests.existsByUserId(userId);
	}

	@Override
	public GuestPage search(String query, int page, int size) {
		var pageable = PageRequest.of(page, size,
				Sort.by("lastName").ascending().and(Sort.by("firstName").ascending()));
		var term = query == null ? "" : query.trim();
		var result = term.isEmpty() ? guests.findAll(pageable)
				: guests.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(term, term, pageable);
		return new GuestPage(result.getContent().stream().map(GuestPersistenceAssembler::toDomain).toList(),
				result.getTotalElements(), page, size);
	}
}
