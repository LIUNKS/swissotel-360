package com.swissotel.swissotel360.rooms.infrastructure.persistence.jpa.adapters;

import com.swissotel.swissotel360.rooms.domain.model.aggregates.Rate;
import com.swissotel.swissotel360.rooms.domain.repositories.RateRepository;
import com.swissotel.swissotel360.rooms.infrastructure.persistence.jpa.assemblers.RoomsPersistenceAssembler;
import com.swissotel.swissotel360.rooms.infrastructure.persistence.jpa.repositories.RatePersistenceRepository;
import com.swissotel.swissotel360.rooms.infrastructure.persistence.jpa.repositories.RoomTypePersistenceRepository;
import org.springframework.stereotype.Repository;
import org.springframework.dao.DataIntegrityViolationException;
import com.swissotel.swissotel360.rooms.domain.exceptions.RateOverlapException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class RateRepositoryImpl implements RateRepository {
	private final RatePersistenceRepository repository;
	private final RoomTypePersistenceRepository roomTypes;

	public RateRepositoryImpl(RatePersistenceRepository repository, RoomTypePersistenceRepository roomTypes) {
		this.repository = repository;
		this.roomTypes = roomTypes;
	}

	public Rate save(Rate rate) {
		var type = roomTypes.findById(rate.roomTypeId()).orElseThrow();
		try {
			return RoomsPersistenceAssembler
					.toDomain(repository.saveAndFlush(RoomsPersistenceAssembler.toEntity(rate, type)));
		} catch (DataIntegrityViolationException ex) {
			throw new RateOverlapException(List.of(rate.validFrom()));
		}
	}

	public List<Rate> findByRoomTypeId(Long id) {
		return repository.findByRoomType_IdOrderByValidFromDesc(id).stream().map(RoomsPersistenceAssembler::toDomain)
				.toList();
	}

	public Optional<Rate> findEffective(Long id, LocalDate date) {
		return repository.findEffective(id, date).map(RoomsPersistenceAssembler::toDomain);
	}

	public List<Rate> findOverlapping(Long id, LocalDate from, LocalDate to) {
		return repository.findOverlapping(id, from, to).stream().map(RoomsPersistenceAssembler::toDomain).toList();
	}

	public Optional<Rate> findById(Long id) {
		return repository.findById(id).map(RoomsPersistenceAssembler::toDomain);
	}

	public void delete(Rate rate) {
		repository.deleteById(rate.id());
	}

	public boolean existsByRoomTypeId(Long id) {
		return repository.existsByRoomType_Id(id);
	}
}
