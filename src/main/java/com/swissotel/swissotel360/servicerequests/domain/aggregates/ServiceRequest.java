package com.swissotel.swissotel360.servicerequests.domain.aggregates;

import com.swissotel.swissotel360.servicerequests.domain.events.ServiceRequestCompleted;
import com.swissotel.swissotel360.servicerequests.domain.exceptions.InvalidServiceRequestTransitionException;
import com.swissotel.swissotel360.servicerequests.domain.model.ServiceRequestId;
import com.swissotel.swissotel360.servicerequests.domain.model.ServiceRequestStatus;
import com.swissotel.swissotel360.servicerequests.domain.model.ServiceType;
import com.swissotel.swissotel360.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import java.math.BigDecimal;
import java.time.Instant;

public final class ServiceRequest extends AbstractDomainAggregateRoot<ServiceRequest> {
	private final ServiceRequestId id;
	private final Long dbId;
	private final Long stayId;
	private final ServiceType serviceType;
	private final String description;
	private final Instant requestedAt;
	private ServiceRequestStatus status;
	private Instant completedAt;
	private Long handledBy;

	private ServiceRequest(ServiceRequestId id, Long dbId, Long stayId, ServiceType serviceType, String description,
			ServiceRequestStatus status, Instant requestedAt, Instant completedAt, Long handledBy) {
		if (id == null || stayId == null || serviceType == null || status == null || requestedAt == null)
			throw new IllegalArgumentException("Service request fields are required");
		this.id = id;
		this.dbId = dbId;
		this.stayId = stayId;
		this.serviceType = serviceType;
		this.description = description;
		this.status = status;
		this.requestedAt = requestedAt;
		this.completedAt = completedAt;
		this.handledBy = handledBy;
	}

	/** The caller guarantees the stay is active. */
	public static ServiceRequest register(Long stayId, ServiceType serviceType, String description, Instant now) {
		return new ServiceRequest(ServiceRequestId.newId(), null, stayId, serviceType,
				description == null || description.isBlank() ? null : description.trim(),
				ServiceRequestStatus.REGISTERED, now, null, null);
	}

	public static ServiceRequest restore(ServiceRequestId id, Long dbId, Long stayId, ServiceType serviceType,
			String description, ServiceRequestStatus status, Instant requestedAt, Instant completedAt,
			Long handledBy) {
		return new ServiceRequest(id, dbId, stayId, serviceType, description, status, requestedAt, completedAt,
				handledBy);
	}

	public void startProgress(Long userId) {
		if (status != ServiceRequestStatus.REGISTERED)
			throw new InvalidServiceRequestTransitionException(status.code(), ServiceRequestStatus.IN_PROGRESS.code());
		status = ServiceRequestStatus.IN_PROGRESS;
		handledBy = userId;
	}

	/** @param chargeAmount optional, must be >= 0 */
	public void complete(Long userId, BigDecimal chargeAmount, Instant now) {
		if (status == ServiceRequestStatus.COMPLETED)
			throw new InvalidServiceRequestTransitionException(status.code(), ServiceRequestStatus.COMPLETED.code());
		if (chargeAmount != null && chargeAmount.signum() < 0)
			throw new IllegalArgumentException("charge amount must not be negative");
		status = ServiceRequestStatus.COMPLETED;
		handledBy = userId;
		completedAt = now;
		registerDomainEvent(new ServiceRequestCompleted(id, stayId, serviceType.code(), serviceType.name(),
				chargeAmount));
	}

	public ServiceRequestId id() {
		return id;
	}

	public Long dbId() {
		return dbId;
	}

	public Long stayId() {
		return stayId;
	}

	public ServiceType serviceType() {
		return serviceType;
	}

	public String description() {
		return description;
	}

	public ServiceRequestStatus status() {
		return status;
	}

	public Instant requestedAt() {
		return requestedAt;
	}

	public Instant completedAt() {
		return completedAt;
	}

	public Long handledBy() {
		return handledBy;
	}
}
