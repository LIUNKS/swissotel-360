package com.swissotel.swissotel360.servicerequests.infrastructure.persistence.jpa.assemblers;

import com.swissotel.swissotel360.servicerequests.domain.aggregates.ServiceRequest;
import com.swissotel.swissotel360.servicerequests.domain.model.ServiceRequestId;
import com.swissotel.swissotel360.servicerequests.domain.model.ServiceRequestStatus;
import com.swissotel.swissotel360.servicerequests.domain.model.ServiceType;
import com.swissotel.swissotel360.servicerequests.infrastructure.persistence.jpa.entities.ServiceRequestPersistenceEntity;
import com.swissotel.swissotel360.servicerequests.infrastructure.persistence.jpa.entities.ServiceTypePersistenceEntity;

public final class ServiceRequestPersistenceAssembler {
	private ServiceRequestPersistenceAssembler() {
	}

	public static ServiceType toDomain(ServiceTypePersistenceEntity e) {
		return new ServiceType(e.getId(), e.getCode(), e.getName(), e.getOperationalArea());
	}

	public static ServiceRequest toDomain(ServiceRequestPersistenceEntity e, ServiceType type) {
		return ServiceRequest.restore(new ServiceRequestId(e.getPublicId()), e.getId(), e.getStayId(), type,
				e.getDescription(), ServiceRequestStatus.fromCode(e.getStatus()), e.getRequestedAt(),
				e.getCompletedAt(), e.getHandledBy());
	}

	public static ServiceRequestPersistenceEntity toEntity(ServiceRequest a, ServiceRequestPersistenceEntity e) {
		e.setPublicId(a.id().value());
		e.setStayId(a.stayId());
		e.setServiceTypeId(a.serviceType().id());
		e.setDescription(a.description());
		e.setStatus(a.status().code());
		e.setRequestedAt(a.requestedAt());
		e.setCompletedAt(a.completedAt());
		e.setHandledBy(a.handledBy());
		return e;
	}
}
