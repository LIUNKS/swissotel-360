package com.swissotel.swissotel360.reservations.infrastructure.persistence.jpa.adapters;

import com.swissotel.swissotel360.reservations.domain.aggregates.Reservation;
import com.swissotel.swissotel360.reservations.domain.exceptions.DuplicateReservationException;
import com.swissotel.swissotel360.reservations.domain.exceptions.ReservationCodeCollisionException;
import com.swissotel.swissotel360.reservations.domain.model.*;
import com.swissotel.swissotel360.reservations.domain.repositories.ReservationRepository;
import com.swissotel.swissotel360.reservations.infrastructure.persistence.jpa.assemblers.ReservationPersistenceAssembler;
import com.swissotel.swissotel360.reservations.infrastructure.persistence.jpa.entities.ReservationPersistenceEntity;
import com.swissotel.swissotel360.reservations.infrastructure.persistence.jpa.repositories.ReservationPersistenceRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public class ReservationRepositoryImpl implements ReservationRepository {
    private final ReservationPersistenceRepository repository;
    public ReservationRepositoryImpl(ReservationPersistenceRepository repository) { this.repository=repository; }
    @Override public Reservation save(Reservation reservation) {
        var entity=repository.findByPublicId(reservation.id().value()).orElseGet(ReservationPersistenceEntity::new);
        try { return ReservationPersistenceAssembler.toDomain(repository.saveAndFlush(ReservationPersistenceAssembler.toEntity(reservation,entity))); }
        catch (DataIntegrityViolationException ex) {
            if (hasConstraint(ex,"uq_reservations_active_duplicate")) throw new DuplicateReservationException();
            if (hasConstraint(ex,"reservations_reservation_code_key")) throw new ReservationCodeCollisionException();
            throw ex;
        }
    }
    @Override public Optional<Reservation> findById(ReservationId id) { return repository.findByPublicId(id.value()).map(ReservationPersistenceAssembler::toDomain); }
    @Override public Optional<Reservation> findByCode(ReservationCode code) { return repository.findByReservationCode(code.value()).map(ReservationPersistenceAssembler::toDomain); }
    @Override public long countActiveOverlapping(RoomTypeId type,StayPeriod period,Optional<ReservationId> excluding) {
        return repository.countActiveOverlapping(type.value(),period.checkIn(),period.checkOut(),excluding.map(ReservationId::value).orElse(null));
    }
    @Override public List<Reservation> findPendingCreatedBefore(Instant threshold,PaymentType type,int limit) {
        return repository.findExpired(threshold,type.code(),PageRequest.of(0,limit)).stream().map(ReservationPersistenceAssembler::toDomain).toList();
    }
    @Override public boolean existsByCode(ReservationCode code) { return repository.existsByReservationCode(code.value()); }
    @Override public ReservationPage search(ReservationSearch search) {
        var page=repository.search(search.status()==null?null:search.status().code(),search.guestId()==null?null:search.guestId().value(),search.code(),search.checkInFrom(),search.checkInTo(),PageRequest.of(search.page(),search.size(),Sort.by(Sort.Direction.DESC,"createdAt")));
        return new ReservationPage(page.getContent().stream().map(ReservationPersistenceAssembler::toDomain).toList(),page.getTotalElements());
    }
    private boolean hasConstraint(Throwable ex,String name) {
        for (var cause=ex;cause!=null;cause=cause.getCause()) if (cause.getMessage()!=null&&cause.getMessage().contains(name)) return true;
        return false;
    }
}
