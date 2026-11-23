package com.swissotel.swissotel360.billing.infrastructure.http;

import com.swissotel.swissotel360.billing.application.internal.outboundservices.acl.BillingCollaboratorsOutboundService;
import com.swissotel.swissotel360.billing.application.internal.outboundservices.acl.ServiceRequestCollaboratorSnapshot;
import com.swissotel.swissotel360.billing.application.internal.outboundservices.acl.StayCollaboratorSnapshot;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

/** HTTP adapter implementing billing's outbound collaborator port via @HttpExchange. */
@Component
public class OperationsHttpCollaboratorsAdapter implements BillingCollaboratorsOutboundService {

	private final OperationsInternalClient client;

	public OperationsHttpCollaboratorsAdapter(OperationsInternalClient client) {
		this.client = client;
	}

	@Override
	public Optional<StayCollaboratorSnapshot> findStay(UUID stayPublicId) {
		return getOrEmpty(() -> client.getStayByPublicId(stayPublicId));
	}

	@Override
	public Optional<StayCollaboratorSnapshot> findStay(Long stayId) {
		return getOrEmpty(() -> client.getStayById(stayId));
	}

	@Override
	public Optional<ServiceRequestCollaboratorSnapshot> findServiceRequest(Long requestId) {
		return getOrEmpty(() -> client.getServiceRequestById(requestId));
	}

	@Override
	public Optional<ServiceRequestCollaboratorSnapshot> findServiceRequest(UUID requestPublicId) {
		return getOrEmpty(() -> client.getServiceRequestByPublicId(requestPublicId));
	}

	private static <T> Optional<T> getOrEmpty(Supplier<T> call) {
		try {
			return Optional.ofNullable(call.get());
		} catch (HttpClientErrorException.NotFound ignored) {
			return Optional.empty();
		}
	}
}
