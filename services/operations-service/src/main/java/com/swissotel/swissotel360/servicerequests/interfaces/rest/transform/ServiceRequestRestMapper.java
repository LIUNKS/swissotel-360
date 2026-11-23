package com.swissotel.swissotel360.servicerequests.interfaces.rest.transform;

import com.swissotel.swissotel360.servicerequests.application.command.*;
import com.swissotel.swissotel360.servicerequests.application.dto.ServiceRequestView;
import com.swissotel.swissotel360.servicerequests.domain.aggregates.ServiceRequest;
import com.swissotel.swissotel360.servicerequests.domain.model.ServiceRequestStatus;
import com.swissotel.swissotel360.servicerequests.domain.model.ServiceType;
import com.swissotel.swissotel360.servicerequests.interfaces.rest.resources.*;
import java.util.UUID;

public final class ServiceRequestRestMapper {
	private ServiceRequestRestMapper() {
	}

	public static CreateServiceRequestCommand toCommand(CreateServiceRequestResource r) {
		return new CreateServiceRequestCommand(r.stayId(), r.serviceTypeCode(), r.description());
	}

	public static UpdateServiceRequestStatusCommand toCommand(UUID requestId, UpdateServiceRequestStatusResource r) {
		return new UpdateServiceRequestStatusCommand(requestId, ServiceRequestStatus.fromCode(r.status().trim()),
				r.chargeAmount());
	}

	public static ServiceTypeResource toResource(ServiceType t) {
		return new ServiceTypeResource(t.code(), t.name(), t.operationalArea());
	}

	public static CreateServiceRequestResultResource toCreated(ServiceRequest r) {
		return new CreateServiceRequestResultResource(r.id().value(), r.status().code(), r.requestedAt());
	}

	public static ServiceRequestStatusResource toStatus(ServiceRequest r) {
		return new ServiceRequestStatusResource(r.id().value(), r.status().code(), r.completedAt());
	}

	public static ServiceRequestResource toResource(ServiceRequestView v) {
		var r = v.request();
		return new ServiceRequestResource(r.id().value(), v.stayPublicId(), r.serviceType().code(),
				r.serviceType().name(), r.description(), r.status().code(), r.requestedAt(), r.completedAt());
	}
}
