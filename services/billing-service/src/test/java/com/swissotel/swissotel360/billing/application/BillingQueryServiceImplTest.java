package com.swissotel.swissotel360.billing.application;

import com.swissotel.swissotel360.billing.application.internal.outboundservices.acl.BillingCollaboratorsOutboundService;
import com.swissotel.swissotel360.billing.application.internal.outboundservices.acl.ServiceRequestCollaboratorSnapshot;
import com.swissotel.swissotel360.billing.application.internal.outboundservices.acl.StayCollaboratorSnapshot;
import com.swissotel.swissotel360.billing.application.internal.queryservices.BillingQueryServiceImpl;
import com.swissotel.swissotel360.billing.domain.aggregates.Folio;
import com.swissotel.swissotel360.billing.domain.exceptions.BillingStayNotFoundException;
import com.swissotel.swissotel360.billing.domain.exceptions.FolioNotFoundException;
import com.swissotel.swissotel360.billing.domain.model.Charge;
import com.swissotel.swissotel360.billing.domain.model.FolioId;
import com.swissotel.swissotel360.billing.domain.model.FolioStatus;
import com.swissotel.swissotel360.billing.domain.repositories.FolioRepository;
import com.swissotel.swissotel360.billing.domain.repositories.PaymentRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BillingQueryServiceImplTest {

	private final FolioRepository folios = mock(FolioRepository.class);
	private final PaymentRepository payments = mock(PaymentRepository.class);
	private final BillingCollaboratorsOutboundService collaborators = mock(BillingCollaboratorsOutboundService.class);
	private final BillingQueryServiceImpl service = new BillingQueryServiceImpl(folios, payments, collaborators);

	@Test
	void resolvesStayAndServiceRequestPublicIdsWhenBuildingFolioView() {
		var folioPublicId = UUID.randomUUID();
		var stayPublicId = UUID.randomUUID();
		var reqPublicId = UUID.randomUUID();
		var now = Instant.parse("2026-11-22T18:00:00Z");

		var folio = Folio.restore(new FolioId(folioPublicId), 10L, 42L, FolioStatus.OPEN,
				List.of(new Charge(1L, UUID.randomUUID(), 77L, "Room Service", new BigDecimal("25.00"), now)));

		when(folios.findByPublicId(new FolioId(folioPublicId))).thenReturn(Optional.of(folio));
		when(collaborators.findStay(42L)).thenReturn(Optional.of(
				new StayCollaboratorSnapshot(42L, stayPublicId, 5L, 6L, 7L, "active", now, null)));
		when(collaborators.findStay(stayPublicId)).thenReturn(Optional.of(
				new StayCollaboratorSnapshot(42L, stayPublicId, 5L, 6L, 7L, "active", now, null)));
		when(folios.findByStayId(42L)).thenReturn(Optional.of(folio));
		when(collaborators.findServiceRequest(77L)).thenReturn(Optional.of(
				new ServiceRequestCollaboratorSnapshot(77L, reqPublicId, 42L, "room_service", "completed")));

		var byFolio = service.getFolio(folioPublicId);
		assertEquals(stayPublicId, byFolio.stayPublicId());
		assertEquals(reqPublicId, byFolio.serviceRequestPublicIds().get(77L));

		var byStay = service.getFolioByStay(stayPublicId);
		assertEquals(folioPublicId, byStay.folio().id().value());
		assertEquals(reqPublicId, byStay.serviceRequestPublicIds().get(77L));
	}

	@Test
	void throwsDomainExceptionsWhenStayOrFolioIsMissing() {
		var missingStayPublicId = UUID.randomUUID();
		when(collaborators.findStay(missingStayPublicId)).thenReturn(Optional.empty());
		assertThrows(BillingStayNotFoundException.class, () -> service.getFolioByStay(missingStayPublicId));

		var missingFolioPublicId = UUID.randomUUID();
		when(folios.findByPublicId(new FolioId(missingFolioPublicId))).thenReturn(Optional.empty());
		assertThrows(FolioNotFoundException.class, () -> service.getFolio(missingFolioPublicId));
	}
}
