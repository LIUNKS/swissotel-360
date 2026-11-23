package com.swissotel.swissotel360.reservations.application.internal.facade;

import com.swissotel.swissotel360.reservations.application.command.ConfirmReservationCommand;
import com.swissotel.swissotel360.reservations.application.command.CompleteReservationCommand;
import com.swissotel.swissotel360.reservations.application.commandservices.ReservationCommandService;
import com.swissotel.swissotel360.reservations.application.dto.ReservationSnapshot;
import com.swissotel.swissotel360.reservations.application.facade.ReservationFacade;
import com.swissotel.swissotel360.reservations.domain.exceptions.ReservationNotFoundException;
import com.swissotel.swissotel360.reservations.domain.aggregates.Reservation;
import com.swissotel.swissotel360.reservations.domain.model.ReservationCode;
import com.swissotel.swissotel360.reservations.domain.repositories.ReservationRepository;

public class ReservationsContextFacadeImpl implements ReservationFacade {
	private final ReservationRepository repository;
	private final ReservationCommandService commands;

	public ReservationsContextFacadeImpl(ReservationRepository repository, ReservationCommandService commands) {
		this.repository = repository;
		this.commands = commands;
	}

	@Override
	public ReservationSnapshot getForCheckIn(Long reservationId) {
		var r = repository.findByInternalId(reservationId)
				.orElseThrow(() -> new ReservationNotFoundException(reservationId));
		return snapshot(r);
	}

	@Override
	public java.util.Optional<ReservationSnapshot> findByCode(String code) {
		return repository.findByCode(new ReservationCode(code)).map(this::snapshot);
	}

	private ReservationSnapshot snapshot(Reservation r) {
		return new ReservationSnapshot(r.id().value(), r.code().value(), r.guestId(), r.roomTypeId(),
				r.period().checkIn(), r.period().checkOut(), r.status().code(),
				repository.findInternalId(r.id()).orElseThrow());
	}

	@Override
	public void markCompleted(Long reservationId) {
		var r = repository.findByInternalId(reservationId)
				.orElseThrow(() -> new ReservationNotFoundException(reservationId));
		commands.complete(new CompleteReservationCommand(r.id().value()));
	}

	@Override
	public void confirmPrepaid(Long reservationId) {
		var r = repository.findByInternalId(reservationId)
				.orElseThrow(() -> new ReservationNotFoundException(reservationId));
		commands.confirm(new ConfirmReservationCommand(r.id().value()));
	}
}
