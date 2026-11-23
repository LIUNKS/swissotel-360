package com.swissotel.swissotel360.servicerequests.domain.repositories;

import com.swissotel.swissotel360.servicerequests.domain.aggregates.ServiceRequest;
import com.swissotel.swissotel360.servicerequests.domain.model.ServiceRequestId;
import com.swissotel.swissotel360.servicerequests.domain.model.ServiceRequestStatus;
import com.swissotel.swissotel360.servicerequests.domain.model.ServiceType;
import java.util.List;
import java.util.Optional;

public interface ServiceRequestRepository {
	ServiceRequest save(ServiceRequest request);

	Optional<ServiceRequest> findByPublicId(ServiceRequestId id);

	Optional<ServiceRequest> findById(Long id);

	/** @param stayId optional internal stay id @param status optional filter */
	List<ServiceRequest> findAll(Long stayId, ServiceRequestStatus status);

	Optional<ServiceType> findServiceTypeByCode(String code);

	List<ServiceType> findAllServiceTypes();
}
