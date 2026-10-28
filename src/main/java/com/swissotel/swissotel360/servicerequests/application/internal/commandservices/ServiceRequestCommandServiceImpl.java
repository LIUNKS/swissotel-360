package com.swissotel.swissotel360.servicerequests.application.internal.commandservices;

import com.swissotel.swissotel360.servicerequests.application.command.*;
import com.swissotel.swissotel360.servicerequests.application.commandservices.ServiceRequestCommandService;
import com.swissotel.swissotel360.servicerequests.application.internal.outboundservices.acl.ServiceRequestCollaboratorsOutboundService;
import com.swissotel.swissotel360.servicerequests.application.port.ServiceRequestEventPublisher;
import com.swissotel.swissotel360.servicerequests.domain.aggregates.ServiceRequest;
import com.swissotel.swissotel360.servicerequests.domain.exceptions.*;
import com.swissotel.swissotel360.servicerequests.domain.model.ServiceRequestId;
import com.swissotel.swissotel360.servicerequests.domain.model.ServiceRequestStatus;
import com.swissotel.swissotel360.servicerequests.domain.repositories.ServiceRequestRepository;
import java.time.Clock;
import java.time.Instant;

public final class ServiceRequestCommandServiceImpl implements ServiceRequestCommandService {
	private final ServiceRequestRepository requests;
	private final ServiceRequestCollaboratorsOutboundService collaborators;
	private final ServiceRequestEventPublisher events;
	private final Clock clock;

	public ServiceRequestCommandServiceImpl(ServiceRequestRepository requests,
			ServiceRequestCollaboratorsOutboundService collaborators, ServiceRequestEventPublisher events,
			Clock clock) {
		this.requests = requests;
		this.collaborators = collaborators;
		this.events = events;
		this.clock = clock;
	}

	@Override
	public ServiceRequest create(CreateServiceRequestCommand command) {
		var stay = collaborators.findStay(command.stayPublicId())
				.orElseThrow(() -> new ServiceRequestStayNotFoundException(command.stayPublicId()));
		if (!"active".equals(stay.status()))
			throw new StayNotActiveForRequestException(command.stayPublicId());
		var type = requests.findServiceTypeByCode(command.serviceTypeCode().trim())
				.orElseThrow(() -> new ServiceTypeNotFoundException(command.serviceTypeCode()));
		return requests.save(ServiceRequest.register(stay.id(), type, command.description(), Instant.now(clock)));
	}

	@Override
	public ServiceRequest updateStatus(UpdateServiceRequestStatusCommand command) {
		var request = requests.findByPublicId(new ServiceRequestId(command.requestPublicId()))
				.orElseThrow(() -> new ServiceRequestNotFoundException(command.requestPublicId()));
		var userId = collaborators.currentUserId();
		switch (command.status()) {
			case IN_PROGRESS -> request.startProgress(userId);
			case COMPLETED -> request.complete(userId, command.chargeAmount(), Instant.now(clock));
			case REGISTERED -> throw new InvalidServiceRequestTransitionException(request.status().code(),
					ServiceRequestStatus.REGISTERED.code());
		}
		var saved = requests.save(request);
		events.publishAfterCommit(saved, request.domainEvents());
		return saved;
	}
}
