package com.swissotel.swissotel360.servicerequests.infrastructure.transaction;

import com.swissotel.swissotel360.servicerequests.application.command.*;
import com.swissotel.swissotel360.servicerequests.application.commandservices.ServiceRequestCommandService;
import com.swissotel.swissotel360.servicerequests.domain.aggregates.ServiceRequest;
import org.springframework.transaction.support.TransactionTemplate;

/** Infrastructure decorator keeps transaction mechanics out of the application layer. */
public final class TransactionalServiceRequestCommandService implements ServiceRequestCommandService {
	private final ServiceRequestCommandService delegate;
	private final TransactionTemplate transactions;

	public TransactionalServiceRequestCommandService(ServiceRequestCommandService delegate,
			TransactionTemplate transactions) {
		this.delegate = delegate;
		this.transactions = transactions;
	}

	@Override
	public ServiceRequest create(CreateServiceRequestCommand c) {
		return transactions.execute(s -> delegate.create(c));
	}

	@Override
	public ServiceRequest updateStatus(UpdateServiceRequestStatusCommand c) {
		return transactions.execute(s -> delegate.updateStatus(c));
	}
}
