package com.swissotel.swissotel360.stays.domain.repositories;

import com.swissotel.swissotel360.stays.domain.aggregates.Stay;
import com.swissotel.swissotel360.stays.domain.model.StayId;
import com.swissotel.swissotel360.stays.domain.model.StayStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StayRepository {
	Stay save(Stay stay);

	Optional<Stay> findByPublicId(StayId id);

	Optional<Stay> findById(Long id);

	Optional<Stay> findByDigitalKeyPublicId(UUID keyPublicId);

	boolean existsByReservationId(Long reservationId);

	/** @param status optional filter, null returns every stay */
	List<Stay> findAll(StayStatus status);
}
