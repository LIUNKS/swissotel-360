package com.swissotel.swissotel360.servicerequests.application;

import com.swissotel.swissotel360.servicerequests.application.command.CreateServiceRequestCommand;
import com.swissotel.swissotel360.servicerequests.application.internal.commandservices.ServiceRequestCommandServiceImpl;
import com.swissotel.swissotel360.servicerequests.application.internal.outboundservices.acl.ServiceRequestCollaboratorsOutboundService;
import com.swissotel.swissotel360.servicerequests.application.internal.queryservices.ServiceRequestQueryServiceImpl;
import com.swissotel.swissotel360.servicerequests.application.port.ServiceRequestEventPublisher;
import com.swissotel.swissotel360.servicerequests.domain.aggregates.ServiceRequest;
import com.swissotel.swissotel360.servicerequests.domain.exceptions.ServiceRequestNotFoundException;
import com.swissotel.swissotel360.servicerequests.domain.exceptions.ServiceRequestStayNotFoundException;
import com.swissotel.swissotel360.servicerequests.domain.model.ServiceRequestId;
import com.swissotel.swissotel360.servicerequests.domain.model.ServiceRequestStatus;
import com.swissotel.swissotel360.servicerequests.domain.model.ServiceType;
import com.swissotel.swissotel360.servicerequests.domain.repositories.ServiceRequestRepository;
import com.swissotel.swissotel360.stays.interfaces.acl.StayContextSnapshot;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ServiceRequestOwnershipTest {

	private static final Instant NOW = Instant.parse("2026-11-20T12:00:00Z");
	private static final Clock FIXED_CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);

	private final ServiceRequestRepository repository = mock(ServiceRequestRepository.class);
	private final ServiceRequestCollaboratorsOutboundService collaborators = mock(
			ServiceRequestCollaboratorsOutboundService.class);
	private final ServiceRequestEventPublisher events = mock(ServiceRequestEventPublisher.class);

	private final ServiceRequestCommandServiceImpl commandService = new ServiceRequestCommandServiceImpl(repository,
			collaborators, events, FIXED_CLOCK);
	private final ServiceRequestQueryServiceImpl queryService = new ServiceRequestQueryServiceImpl(repository,
			collaborators);

	@Test
	void guestCanCreateListAndViewOwnServiceRequests() {
		var ownStayPublicId = UUID.randomUUID();
		var otherStayPublicId = UUID.randomUUID();
		var ownStay = new StayContextSnapshot(10L, ownStayPublicId, 100L, 50L, 1L, "active", NOW, null);
		var otherStay = new StayContextSnapshot(20L, otherStayPublicId, 200L, 99L, 2L, "active", NOW, null);
		var serviceType = new ServiceType(1L, "room_service", "Room Service", "F&B");
		var ownRequest = ServiceRequest.register(10L, serviceType, "Coffee", NOW);
		var otherRequest = ServiceRequest.register(20L, serviceType, "Towels", NOW);

		when(collaborators.isCurrentUserGuest()).thenReturn(true);
		when(collaborators.currentGuestId()).thenReturn(Optional.of(50L));
		when(collaborators.findStay(ownStayPublicId)).thenReturn(Optional.of(ownStay));
		when(collaborators.findStay(10L)).thenReturn(Optional.of(ownStay));
		when(collaborators.findStay(20L)).thenReturn(Optional.of(otherStay));
		when(repository.findServiceTypeByCode("room_service")).thenReturn(Optional.of(serviceType));
		when(repository.save(any(ServiceRequest.class))).thenAnswer(inv -> inv.getArgument(0));
		when(repository.findByPublicId(new ServiceRequestId(ownRequest.id().value()))).thenReturn(Optional.of(ownRequest));
		when(repository.findAll(null, null)).thenReturn(List.of(ownRequest, otherRequest));
		when(repository.findAll(10L, ServiceRequestStatus.REGISTERED)).thenReturn(List.of(ownRequest));

		var created = commandService.create(new CreateServiceRequestCommand(ownStayPublicId, "room_service", "Coffee"));
		assertNotNull(created);
		assertEquals(10L, created.stayId());

		var viewed = queryService.getView(ownRequest.id().value());
		assertEquals(ownStayPublicId, viewed.stayPublicId());

		var listedAll = queryService.list(null, null);
		assertEquals(1, listedAll.size());
		assertEquals(ownRequest.id(), listedAll.getFirst().request().id());

		var listedByStay = queryService.list(ownStayPublicId, ServiceRequestStatus.REGISTERED);
		assertEquals(1, listedByStay.size());
		assertEquals(ownStayPublicId, listedByStay.getFirst().stayPublicId());
	}

	@Test
	void guestCannotCreateFilterOrViewAnotherGuestsServiceRequests() {
		var otherStayPublicId = UUID.randomUUID();
		var otherStay = new StayContextSnapshot(20L, otherStayPublicId, 200L, 99L, 2L, "active", NOW, null);
		var serviceType = new ServiceType(1L, "room_service", "Room Service", "F&B");
		var otherRequest = ServiceRequest.register(20L, serviceType, "Towels", NOW);

		when(collaborators.isCurrentUserGuest()).thenReturn(true);
		when(collaborators.currentGuestId()).thenReturn(Optional.of(50L));
		when(collaborators.findStay(otherStayPublicId)).thenReturn(Optional.of(otherStay));
		when(collaborators.findStay(20L)).thenReturn(Optional.of(otherStay));
		when(repository.findByPublicId(new ServiceRequestId(otherRequest.id().value())))
				.thenReturn(Optional.of(otherRequest));

		assertThrows(ServiceRequestStayNotFoundException.class,
				() -> commandService.create(new CreateServiceRequestCommand(otherStayPublicId, "room_service", "Hack")));
		verify(repository, never()).save(any());

		assertThrows(ServiceRequestStayNotFoundException.class,
				() -> queryService.list(otherStayPublicId, null));

		assertThrows(ServiceRequestNotFoundException.class,
				() -> queryService.getView(otherRequest.id().value()));
	}

	@Test
	void staffCanCreateAndListAnyGuestsServiceRequests() {
		var otherStayPublicId = UUID.randomUUID();
		var otherStay = new StayContextSnapshot(20L, otherStayPublicId, 200L, 99L, 2L, "active", NOW, null);
		var serviceType = new ServiceType(1L, "room_service", "Room Service", "F&B");
		var otherRequest = ServiceRequest.register(20L, serviceType, "Towels", NOW);

		when(collaborators.isCurrentUserGuest()).thenReturn(false);
		when(collaborators.findStay(otherStayPublicId)).thenReturn(Optional.of(otherStay));
		when(collaborators.findStay(20L)).thenReturn(Optional.of(otherStay));
		when(repository.findServiceTypeByCode("room_service")).thenReturn(Optional.of(serviceType));
		when(repository.save(any(ServiceRequest.class))).thenAnswer(inv -> inv.getArgument(0));
		when(repository.findAll(null, null)).thenReturn(List.of(otherRequest));

		var created = commandService.create(new CreateServiceRequestCommand(otherStayPublicId, "room_service", "Staff order"));
		assertEquals(20L, created.stayId());

		var listed = queryService.list(null, null);
		assertEquals(1, listed.size());
		assertEquals(otherStayPublicId, listed.getFirst().stayPublicId());
	}
}
