package com.swissotel.swissotel360.stays.infrastructure.persistence.jpa.adapters;

import com.swissotel.swissotel360.stays.domain.aggregates.Stay;
import com.swissotel.swissotel360.stays.domain.exceptions.ReservationNotEligibleForCheckInException;
import com.swissotel.swissotel360.stays.domain.exceptions.RoomNotAvailableException;
import com.swissotel.swissotel360.stays.domain.model.StayId;
import com.swissotel.swissotel360.stays.domain.model.StayStatus;
import com.swissotel.swissotel360.stays.domain.repositories.StayRepository;
import com.swissotel.swissotel360.stays.infrastructure.persistence.jpa.assemblers.StayPersistenceAssembler;
import com.swissotel.swissotel360.stays.infrastructure.persistence.jpa.entities.DigitalKeyPersistenceEntity;
import com.swissotel.swissotel360.stays.infrastructure.persistence.jpa.entities.StayPersistenceEntity;
import com.swissotel.swissotel360.stays.infrastructure.persistence.jpa.repositories.DigitalKeyPersistenceRepository;
import com.swissotel.swissotel360.stays.infrastructure.persistence.jpa.repositories.StayPersistenceRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class StayRepositoryImpl implements StayRepository {
	private final StayPersistenceRepository stays;
	private final DigitalKeyPersistenceRepository keys;

	public StayRepositoryImpl(StayPersistenceRepository stays, DigitalKeyPersistenceRepository keys) {
		this.stays = stays;
		this.keys = keys;
	}

	@Override
	public Stay save(Stay stay) {
		var entity = stay.dbId() == null ? new StayPersistenceEntity() : stays.findById(stay.dbId()).orElseThrow();
		try {
			entity = stays.saveAndFlush(StayPersistenceAssembler.toEntity(stay, entity));
			// Persist existing keys (revocations) before inserting the new active key so the partial unique index
			// uq_digital_keys_active_stay is never violated within a single flush.
			for (var key : stay.keys())
				if (key.id() != null)
					keys.saveAndFlush(StayPersistenceAssembler.toEntity(key, entity.getId(),
							keys.findById(key.id()).orElseThrow()));
			for (var key : stay.keys())
				if (key.id() == null)
					keys.saveAndFlush(
							StayPersistenceAssembler.toEntity(key, entity.getId(), new DigitalKeyPersistenceEntity()));
		} catch (DataIntegrityViolationException ex) {
			if (hasConstraint(ex, "uq_stays_active_room"))
				throw new RoomNotAvailableException("room already has an active stay");
			if (hasConstraint(ex, "stays_reservation_id_key"))
				throw new ReservationNotEligibleForCheckInException("already checked in");
			throw ex;
		}
		return toDomain(entity);
	}

	@Override
	public Optional<Stay> findByPublicId(StayId id) {
		return stays.findByPublicId(id.value()).map(this::toDomain);
	}

	@Override
	public Optional<Stay> findById(Long id) {
		return stays.findById(id).map(this::toDomain);
	}

	@Override
	public Optional<Stay> findByDigitalKeyPublicId(UUID keyPublicId) {
		return keys.findByPublicId(keyPublicId).flatMap(k -> stays.findById(k.getStayId())).map(this::toDomain);
	}

	@Override
	public boolean existsByReservationId(Long reservationId) {
		return stays.existsByReservationId(reservationId);
	}

	@Override
	public List<Stay> findAll(StayStatus status) {
		// ponytail: unpaged and one key query per stay; add paging/join fetch if the stay volume grows
		var found = status == null ? stays.findAllByOrderByCheckedInAtDesc()
				: stays.findByStatusOrderByCheckedInAtDesc(status.code());
		return found.stream().map(this::toDomain).toList();
	}

	private Stay toDomain(StayPersistenceEntity e) {
		return StayPersistenceAssembler.toDomain(e, keys.findByStayIdOrderByIdAsc(e.getId()));
	}

	private boolean hasConstraint(Throwable ex, String name) {
		for (var cause = ex; cause != null; cause = cause.getCause())
			if (cause.getMessage() != null && cause.getMessage().contains(name))
				return true;
		return false;
	}
}
