package com.swissotel.swissotel360.reservations.infrastructure.transaction;

import com.swissotel.swissotel360.reservations.application.command.*;
import com.swissotel.swissotel360.reservations.application.commandservices.ReservationCommandService;
import com.swissotel.swissotel360.reservations.application.dto.ReservationResult;
import com.swissotel.swissotel360.reservations.domain.aggregates.Reservation;
import com.swissotel.swissotel360.reservations.domain.exceptions.DuplicateReservationException;
import com.swissotel.swissotel360.reservations.domain.exceptions.ReservationCodeCollisionException;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Infrastructure decorator keeps transaction mechanics out of the application
 * layer.
 */
public final class TransactionalReservationCommandService implements ReservationCommandService {
	private final ReservationCommandService delegate;
	private final TransactionTemplate transactions;

	public TransactionalReservationCommandService(ReservationCommandService delegate,
			TransactionTemplate transactions) {
		this.delegate = delegate;
		this.transactions = transactions;
	}

	@Override
	public ReservationResult create(CreateReservationCommand c) {
		for (int attempt = 0; attempt < 5; attempt++) {
			try {
				return transactions.execute(s -> delegate.create(c));
			} catch (ReservationCodeCollisionException ex) {
				/* retry in a fresh transaction after the failed insert rollback */ }
		}
		throw new DuplicateReservationException();
	}

	@Override
	public ReservationResult modify(ModifyReservationCommand c) {
		return transactions.execute(s -> delegate.modify(c));
	}

	@Override
	public Reservation confirm(ConfirmReservationCommand c) {
		return transactions.execute(s -> delegate.confirm(c));
	}

	@Override
	public Reservation cancel(CancelReservationCommand c) {
		return transactions.execute(s -> delegate.cancel(c));
	}

	@Override
	public Reservation complete(CompleteReservationCommand c) {
		return transactions.execute(s -> delegate.complete(c));
	}
}
