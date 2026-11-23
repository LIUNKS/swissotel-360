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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
		var request = findByPublicId(requestId).orElseThrow(() -> new ServiceRequestNotFoundException(requestId));
		var stay = collaborators.findStay(request.stayId());
		if (collaborators.isCurrentUserGuest()) {
			var ownGuestId = collaborators.currentGuestId();
			if (ownGuestId.isEmpty() || stay.isEmpty() || !ownGuestId.get().equals(stay.get().guestId())) {
				throw new ServiceRequestNotFoundException(requestId);
			}
		}
		return new ServiceRequestView(request, stay.map(StayContextSnapshot::publicId).orElse(null));
	}

	@Override
	public List<ServiceRequestView> list(UUID stayPublicId, ServiceRequestStatus status) {
		if (collaborators.isCurrentUserGuest()) {
			var ownGuestId = collaborators.currentGuestId();
			if (stayPublicId != null) {
				var stay = collaborators.findStay(stayPublicId)
						.orElseThrow(() -> new ServiceRequestStayNotFoundException(stayPublicId));
				if (ownGuestId.isEmpty() || !ownGuestId.get().equals(stay.guestId())) {
					throw new ServiceRequestStayNotFoundException(stayPublicId);
				}
				return requests.findAll(stay.id(), status).stream()
						.map(r -> new ServiceRequestView(r, stay.publicId()))
						.toList();
			}
			if (ownGuestId.isEmpty()) {
				return List.of();
			}
			Map<Long, Optional<StayContextSnapshot>> stayCache = new HashMap<>();
			return requests.findAll(null, status).stream()
					.map(r -> new RequestWithStay(r, stayCache.computeIfAbsent(r.stayId(), collaborators::findStay)))
					.filter(item -> item.stay().isPresent() && ownGuestId.get().equals(item.stay().get().guestId()))
					.map(item -> new ServiceRequestView(item.request(), item.stay().get().publicId()))
					.toList();
		}

		Long stayId = stayPublicId == null ? null
				: collaborators.findStay(stayPublicId).map(StayContextSnapshot::id)
						.orElseThrow(() -> new ServiceRequestStayNotFoundException(stayPublicId));
		Map<Long, Optional<StayContextSnapshot>> stayCache = new HashMap<>();
		return requests.findAll(stayId, status).stream()
				.map(r -> new ServiceRequestView(r,
						stayCache.computeIfAbsent(r.stayId(), collaborators::findStay)
								.map(StayContextSnapshot::publicId).orElse(null)))
				.toList();
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

	private record RequestWithStay(ServiceRequest request, Optional<StayContextSnapshot> stay) {
	}
}
