package com.swissotel.swissotel360.servicerequests.domain.model;

import java.util.UUID;

public record ServiceRequestId(UUID value) {
	public ServiceRequestId {
		if (value == null)
			throw new IllegalArgumentException("service request id is required");
	}

	public static ServiceRequestId newId() {
		return new ServiceRequestId(UUID.randomUUID());
	}
}
