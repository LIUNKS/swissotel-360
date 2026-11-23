package com.swissotel.swissotel360.servicerequests.domain.model;

public record ServiceType(Long id, String code, String name, String operationalArea) {
	public ServiceType {
		if (code == null || code.isBlank() || name == null || name.isBlank())
			throw new IllegalArgumentException("service type code and name are required");
	}
}
