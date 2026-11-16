package com.swissotel.swissotel360.servicerequests.application.internal.queryservices;

import com.swissotel.swissotel360.servicerequests.application.dto.ServiceRequestView;
import com.swissotel.swissotel360.servicerequests.application.internal.outboundservices.acl.ServiceRequestCollaboratorsOutboundService;
import com.swissotel.swissotel360.servicerequests.application.queryservices.ServiceRequestQueryService;
import com.swissotel.swissotel360.servicerequests.domain.aggregates.ServiceRequest;
import com.swissotel.swissotel360.servicerequests.domain.exceptions.ServiceRequestNotFoundException;
import com.swissotel.swissotel360.servicerequests.domain.exceptions.ServiceRequestStayNotFoundException;
import com.swissotel.swissotel360.servicerequests.domain.model.ServiceRequestId;
import com.swissotel.swissotel360.servicerequests.domain.model.ServiceRequestStatus;
import com.swissotel.swissotel360.servicerequests.domain.model.ServiceType;
import com.swissotel.swissotel360.servicerequests.domain.repositories.ServiceRequestRepository;
import com.swissotel.swissotel360.stays.interfaces.acl.StayContextSnapshot;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class ServiceRequestQueryServiceImpl implements ServiceRequestQueryService {
	private final ServiceRequestRepository requests;
	private final ServiceRequestCollaboratorsOutboundService collaborators;

	public ServiceRequestQueryServiceImpl(ServiceRequestRepository requests,
			ServiceRequestCollaboratorsOutboundService collaborators) {
		this.requests = requests;
		this.collaborators = collaborators;
	}

	@Override
	public ServiceRequestView getView(UUID requestId) {
		return view(findByPublicId(requestId).orElseThrow(() -> new ServiceRequestNotFoundException(requestId)));
	}

	@Override
	public List<ServiceRequestView> list(UUID stayPublicId, ServiceRequestStatus status) {
		Long stayId = stayPublicId == null ? null
				: collaborators.findStay(stayPublicId).map(StayContextSnapshot::id)
						.orElseThrow(() -> new ServiceRequestStayNotFoundException(stayPublicId));
		// ponytail: one stay lookup per request, batch by stay ids if listing volume grows
		return requests.findAll(stayId, status).stream().map(this::view).toList();
	}

	@Override
	public List<ServiceType> listServiceTypes() {
		return requests.findAllServiceTypes();
	}

	@Override
	public Optional<ServiceRequest> findByPublicId(UUID id) {
		return requests.findByPublicId(new ServiceRequestId(id));
	}

	@Override
	public Optional<ServiceRequest> findById(Long id) {
		return requests.findById(id);
	}

	private ServiceRequestView view(ServiceRequest r) {
		return new ServiceRequestView(r,
				collaborators.findStay(r.stayId()).map(StayContextSnapshot::publicId).orElse(null));
	}
}
