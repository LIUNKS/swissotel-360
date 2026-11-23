package com.swissotel.swissotel360.servicerequests.interfaces.acl;

import com.swissotel.swissotel360.servicerequests.application.queryservices.ServiceRequestQueryService;
import com.swissotel.swissotel360.servicerequests.domain.aggregates.ServiceRequest;
import org.springframework.stereotype.Component;
import java.util.Optional;
import java.util.UUID;

@Component
public class ServiceRequestsContextFacadeImpl implements ServiceRequestsContextFacade {
	private final ServiceRequestQueryService requests;

	public ServiceRequestsContextFacadeImpl(ServiceRequestQueryService requests) {
		this.requests = requests;
	}

	@Override
	public Optional<ServiceRequestContextSnapshot> findByPublicId(UUID id) {
		return requests.findByPublicId(id).map(ServiceRequestsContextFacadeImpl::snapshot);
	}

	@Override
	public Optional<ServiceRequestContextSnapshot> findById(Long id) {
		return requests.findById(id).map(ServiceRequestsContextFacadeImpl::snapshot);
	}

	private static ServiceRequestContextSnapshot snapshot(ServiceRequest r) {
		return new ServiceRequestContextSnapshot(r.dbId(), r.id().value(), r.stayId(), r.serviceType().code(),
				r.status().code());
	}
}
