package com.swissotel.swissotel360.reservations.infrastructure.persistence.jpa.adapters;

import com.swissotel.swissotel360.reservations.domain.aggregates.Reservation;
import com.swissotel.swissotel360.reservations.domain.exceptions.DuplicateReservationException;
import com.swissotel.swissotel360.reservations.domain.exceptions.ReservationCodeCollisionException;
import com.swissotel.swissotel360.reservations.domain.model.*;
import com.swissotel.swissotel360.reservations.domain.repositories.ReservationRepository;
import com.swissotel.swissotel360.reservations.infrastructure.persistence.jpa.assemblers.ReservationPersistenceAssembler;
import com.swissotel.swissotel360.reservations.infrastructure.persistence.jpa.entities.ReservationPersistenceEntity;
import com.swissotel.swissotel360.reservations.infrastructure.persistence.jpa.repositories.ReservationPersistenceRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Repository
public class ReservationRepositoryImpl implements ReservationRepository {
	private final ReservationPersistenceRepository repository;

	public ReservationRepositoryImpl(ReservationPersistenceRepository repository) {
		this.repository = repository;
	}

	@Override
	public Reservation save(Reservation reservation) {
		var entity = repository.findByPublicId(reservation.id().value()).orElseGet(ReservationPersistenceEntity::new);
		try {
			return ReservationPersistenceAssembler
					.toDomain(repository.saveAndFlush(ReservationPersistenceAssembler.toEntity(reservation, entity)));
		} catch (DataIntegrityViolationException ex) {
			if (hasConstraint(ex, "uq_reservations_active_duplicate"))
				throw new DuplicateReservationException();
			if (hasConstraint(ex, "reservations_reservation_code_key"))
				throw new ReservationCodeCollisionException();
			throw ex;
		}
	}

	@Override
	public Optional<Reservation> findById(ReservationId id) {
		return repository.findByPublicId(id.value()).map(ReservationPersistenceAssembler::toDomain);
	}

	@Override
	public Optional<Reservation> findByCode(ReservationCode code) {
		return repository.findByReservationCode(code.value()).map(ReservationPersistenceAssembler::toDomain);
	}

	@Override
	public Optional<Long> findInternalId(ReservationId id) {
		return repository.findByPublicId(id.value()).map(ReservationPersistenceEntity::getId);
	}

	@Override
	public long countActiveOverlapping(RoomTypeId type, StayPeriod period, Optional<ReservationId> excluding) {
		if (excluding != null && excluding.isPresent()) {
			return repository.countActiveOverlappingExcluding(type.value(), period.checkIn(), period.checkOut(),
					excluding.get().value());
		}
		return repository.countActiveOverlapping(type.value(), period.checkIn(), period.checkOut());
	}

	@Override
	public List<Reservation> findPendingCreatedBefore(Instant threshold, PaymentType type, int limit) {
		return repository.findExpired(threshold, type.code(), PageRequest.of(0, limit)).stream()
				.map(ReservationPersistenceAssembler::toDomain).toList();
	}

	@Override
	public boolean existsByCode(ReservationCode code) {
		return repository.existsByReservationCode(code.value());
	}

	@Override
	public ReservationPage search(ReservationSearch search) {
		var page = repository.findAll(buildSearchSpecification(search),
				PageRequest.of(search.page(), search.size(), Sort.by(Sort.Direction.DESC, "createdAt")));
		return new ReservationPage(page.getContent().stream().map(ReservationPersistenceAssembler::toDomain).toList(),
				page.getTotalElements());
	}

	private Specification<ReservationPersistenceEntity> buildSearchSpecification(ReservationSearch search) {
		return (root, query, cb) -> {
			List<Predicate> predicates = new ArrayList<>();
			if (search.status() != null) {
				predicates.add(cb.equal(root.get("status"), search.status().code()));
			}
			if (search.guestId() != null) {
				predicates.add(cb.equal(root.get("guestId"), search.guestId().value()));
			}
			if (search.code() != null && !search.code().isBlank()) {
				predicates.add(cb.like(cb.lower(root.<String>get("reservationCode")),
						"%" + search.code().trim().toLowerCase(Locale.ROOT) + "%"));
			}
			if (search.checkInFrom() != null) {
				predicates.add(cb.greaterThanOrEqualTo(root.<LocalDate>get("checkInDate"), search.checkInFrom()));
			}
			if (search.checkInTo() != null) {
				predicates.add(cb.lessThanOrEqualTo(root.<LocalDate>get("checkInDate"), search.checkInTo()));
			}
			return cb.and(predicates.toArray(Predicate[]::new));
		};
	}

	private boolean hasConstraint(Throwable ex, String name) {
		for (var cause = ex; cause != null; cause = cause.getCause())
			if (cause.getMessage() != null && cause.getMessage().contains(name))
				return true;
		return false;
	}
}
