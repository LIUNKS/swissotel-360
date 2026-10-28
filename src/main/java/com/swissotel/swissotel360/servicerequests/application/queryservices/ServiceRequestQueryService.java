package com.swissotel.swissotel360.servicerequests.application.queryservices;

import com.swissotel.swissotel360.servicerequests.application.dto.ServiceRequestView;
import com.swissotel.swissotel360.servicerequests.domain.aggregates.ServiceRequest;
import com.swissotel.swissotel360.servicerequests.domain.model.ServiceRequestStatus;
import com.swissotel.swissotel360.servicerequests.domain.model.ServiceType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ServiceRequestQueryService {
	ServiceRequestView getView(UUID requestId);

	/** Both filters are optional. */
	List<ServiceRequestView> list(UUID stayPublicId, ServiceRequestStatus status);

	List<ServiceType> listServiceTypes();

	Optional<ServiceRequest> findByPublicId(UUID id);

	Optional<ServiceRequest> findById(Long id);
}
