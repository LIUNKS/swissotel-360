package com.swissotel.swissotel360.servicerequests.infrastructure.persistence.jpa.adapters;

import com.swissotel.swissotel360.servicerequests.domain.aggregates.ServiceRequest;
import com.swissotel.swissotel360.servicerequests.domain.model.ServiceRequestId;
import com.swissotel.swissotel360.servicerequests.domain.model.ServiceRequestStatus;
import com.swissotel.swissotel360.servicerequests.domain.model.ServiceType;
import com.swissotel.swissotel360.servicerequests.domain.repositories.ServiceRequestRepository;
import com.swissotel.swissotel360.servicerequests.infrastructure.persistence.jpa.assemblers.ServiceRequestPersistenceAssembler;
import com.swissotel.swissotel360.servicerequests.infrastructure.persistence.jpa.entities.ServiceRequestPersistenceEntity;
import com.swissotel.swissotel360.servicerequests.infrastructure.persistence.jpa.repositories.ServiceRequestPersistenceRepository;
import com.swissotel.swissotel360.servicerequests.infrastructure.persistence.jpa.repositories.ServiceTypePersistenceRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public class ServiceRequestRepositoryImpl implements ServiceRequestRepository {
	private final ServiceRequestPersistenceRepository requests;
	private final ServiceTypePersistenceRepository types;

	public ServiceRequestRepositoryImpl(ServiceRequestPersistenceRepository requests,
			ServiceTypePersistenceRepository types) {
		this.requests = requests;
		this.types = types;
	}

	@Override
	public ServiceRequest save(ServiceRequest request) {
		var entity = request.dbId() == null ? new ServiceRequestPersistenceEntity()
				: requests.findById(request.dbId()).orElseThrow();
		return toDomain(requests.saveAndFlush(ServiceRequestPersistenceAssembler.toEntity(request, entity)));
	}

	@Override
	public Optional<ServiceRequest> findByPublicId(ServiceRequestId id) {
		return requests.findByPublicId(id.value()).map(this::toDomain);
	}

	@Override
	public Optional<ServiceRequest> findById(Long id) {
		return requests.findById(id).map(this::toDomain);
	}

	@Override
	public List<ServiceRequest> findAll(Long stayId, ServiceRequestStatus status) {
		List<ServiceRequestPersistenceEntity> found;
		if (stayId == null)
			found = status == null ? requests.findAllByOrderByRequestedAtDesc()
					: requests.findByStatusOrderByRequestedAtDesc(status.code());
		else
			found = status == null ? requests.findByStayIdOrderByRequestedAtDesc(stayId)
					: requests.findByStayIdAndStatusOrderByRequestedAtDesc(stayId, status.code());
		return found.stream().map(this::toDomain).toList();
	}

	@Override
	public Optional<ServiceType> findServiceTypeByCode(String code) {
		return types.findByCode(code).map(ServiceRequestPersistenceAssembler::toDomain);
	}

	@Override
	public List<ServiceType> findAllServiceTypes() {
		return types.findAllByOrderByNameAsc().stream().map(ServiceRequestPersistenceAssembler::toDomain).toList();
	}

	private ServiceRequest toDomain(ServiceRequestPersistenceEntity e) {
		return ServiceRequestPersistenceAssembler.toDomain(e,
				ServiceRequestPersistenceAssembler.toDomain(types.findById(e.getServiceTypeId()).orElseThrow()));
	}
}
