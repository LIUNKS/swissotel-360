package com.swissotel.swissotel360.reservations.application.internal.facade;
import com.swissotel.swissotel360.reservations.application.command.ConfirmReservationCommand;
import com.swissotel.swissotel360.reservations.application.command.CompleteReservationCommand;
import com.swissotel.swissotel360.reservations.application.commandservices.ReservationCommandService;
import com.swissotel.swissotel360.reservations.application.dto.ReservationSnapshot;
import com.swissotel.swissotel360.reservations.application.facade.ReservationFacade;
import com.swissotel.swissotel360.reservations.domain.exceptions.ReservationNotFoundException;
import com.swissotel.swissotel360.reservations.domain.model.ReservationId;
import com.swissotel.swissotel360.reservations.domain.repositories.ReservationRepository;
import java.util.UUID;
public class ReservationsContextFacadeImpl implements ReservationFacade {
    private final ReservationRepository repository; private final ReservationCommandService commands;
    public ReservationsContextFacadeImpl(ReservationRepository repository,ReservationCommandService commands) { this.repository=repository; this.commands=commands; }
    @Override public ReservationSnapshot getForCheckIn(UUID id) {
        var r=repository.findById(new ReservationId(id)).orElseThrow(()->new ReservationNotFoundException(id));
        return new ReservationSnapshot(r.id().value(),r.code().value(),r.guestId(),r.roomTypeId(),r.period().checkIn(),r.period().checkOut(),r.status().code());
    }
    @Override public void markCompleted(UUID id) { commands.complete(new CompleteReservationCommand(id)); }
    @Override public void confirmPrepaid(UUID id) { commands.confirm(new ConfirmReservationCommand(id)); }
}
