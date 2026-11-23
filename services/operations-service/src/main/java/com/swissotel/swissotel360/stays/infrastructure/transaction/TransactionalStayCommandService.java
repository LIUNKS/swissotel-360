package com.swissotel.swissotel360.stays.infrastructure.transaction;

import com.swissotel.swissotel360.stays.application.command.*;
import com.swissotel.swissotel360.stays.application.commandservices.StayCommandService;
import com.swissotel.swissotel360.stays.application.dto.StayView;
import com.swissotel.swissotel360.stays.domain.aggregates.Stay;
import com.swissotel.swissotel360.stays.domain.model.DigitalKey;
import org.springframework.transaction.support.TransactionTemplate;

/** Infrastructure decorator keeps transaction mechanics out of the application layer. */
public final class TransactionalStayCommandService implements StayCommandService {
	private final StayCommandService delegate;
	private final TransactionTemplate transactions;

	public TransactionalStayCommandService(StayCommandService delegate, TransactionTemplate transactions) {
		this.delegate = delegate;
		this.transactions = transactions;
	}

	@Override
	public StayView checkIn(CheckInCommand c) {
		return transactions.execute(s -> delegate.checkIn(c));
	}

	@Override
	public Stay checkOut(CheckOutCommand c) {
		return transactions.execute(s -> delegate.checkOut(c));
	}

	@Override
	public DigitalKey issueKey(IssueDigitalKeyCommand c) {
		return transactions.execute(s -> delegate.issueKey(c));
	}

	@Override
	public DigitalKey revokeKey(RevokeDigitalKeyCommand c) {
		return transactions.execute(s -> delegate.revokeKey(c));
	}
}
