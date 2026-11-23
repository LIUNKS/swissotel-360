package com.swissotel.swissotel360.rooms.domain.repositories;

import com.swissotel.swissotel360.rooms.domain.model.aggregates.Rate;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RateRepository {
	Rate save(Rate rate);

	List<Rate> findByRoomTypeId(Long roomTypeId);

	Optional<Rate> findEffective(Long roomTypeId, LocalDate date);

	List<Rate> findOverlapping(Long roomTypeId, LocalDate from, LocalDate to);

	Optional<Rate> findById(Long id);

	Optional<Rate> findByPublicId(UUID publicId);

	void delete(Rate rate);

	boolean existsByRoomTypeId(Long roomTypeId);
}
