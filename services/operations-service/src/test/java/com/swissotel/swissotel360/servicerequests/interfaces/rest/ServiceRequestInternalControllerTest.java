package com.swissotel.swissotel360.servicerequests.interfaces.rest;

import com.swissotel.swissotel360.servicerequests.interfaces.acl.ServiceRequestContextSnapshot;
import com.swissotel.swissotel360.servicerequests.interfaces.acl.ServiceRequestsContextFacade;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ServiceRequestInternalControllerTest {

	private final ServiceRequestsContextFacade serviceRequests = mock(ServiceRequestsContextFacade.class);
	private final ServiceRequestInternalController controller = new ServiceRequestInternalController(serviceRequests);

	@Test
	void returnsSnapshotByInternalIdAndByPublicIdOr404WhenMissing() {
		var publicId = UUID.randomUUID();
		var snapshot = new ServiceRequestContextSnapshot(77L, publicId, 42L, "room_service", "completed");

		when(serviceRequests.findById(77L)).thenReturn(Optional.of(snapshot));
		when(serviceRequests.findByPublicId(publicId)).thenReturn(Optional.of(snapshot));
		when(serviceRequests.findById(999L)).thenReturn(Optional.empty());

		var byId = controller.getById(77L);
		assertEquals(HttpStatus.OK, byId.getStatusCode());
		assertEquals(snapshot, byId.getBody());

		var byPublicId = controller.getByPublicId(publicId);
		assertEquals(HttpStatus.OK, byPublicId.getStatusCode());
		assertEquals(snapshot, byPublicId.getBody());

		assertEquals(HttpStatus.NOT_FOUND, controller.getById(999L).getStatusCode());
		assertEquals(HttpStatus.NOT_FOUND, controller.getByPublicId(UUID.randomUUID()).getStatusCode());
	}
}
