package com.swissotel.swissotel360.billing.application.internal.queryservices;

import com.swissotel.swissotel360.billing.application.dto.FolioView;
import com.swissotel.swissotel360.billing.application.internal.outboundservices.acl.BillingCollaboratorsOutboundService;
import com.swissotel.swissotel360.billing.application.internal.outboundservices.acl.StayCollaboratorSnapshot;
import com.swissotel.swissotel360.billing.application.queryservices.BillingQueryService;
import com.swissotel.swissotel360.billing.domain.aggregates.Folio;
import com.swissotel.swissotel360.billing.domain.exceptions.BillingStayNotFoundException;
import com.swissotel.swissotel360.billing.domain.exceptions.FolioNotFoundException;
import com.swissotel.swissotel360.billing.domain.model.Charge;
import com.swissotel.swissotel360.billing.domain.model.FolioId;
import com.swissotel.swissotel360.billing.domain.model.PaymentMethod;
import com.swissotel.swissotel360.billing.domain.repositories.FolioRepository;
import com.swissotel.swissotel360.billing.domain.repositories.PaymentRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class BillingQueryServiceImpl implements BillingQueryService {
	private final FolioRepository folios;
	private final PaymentRepository payments;
	private final BillingCollaboratorsOutboundService collaborators;

	public BillingQueryServiceImpl(FolioRepository folios, PaymentRepository payments,
			BillingCollaboratorsOutboundService collaborators) {
		this.folios = folios;
		this.payments = payments;
		this.collaborators = collaborators;
	}

	@Override
	public FolioView getFolio(UUID folioId) {
		var folio = folios.findByPublicId(new FolioId(folioId)).orElseThrow(() -> new FolioNotFoundException(folioId));
		return view(folio, stayPublicId(folio));
	}

	@Override
	public FolioView getFolioByStay(UUID stayPublicId) {
		var stay = collaborators.findStay(stayPublicId)
				.orElseThrow(() -> new BillingStayNotFoundException(stayPublicId));
		var folio = folios.findByStayId(stay.id()).orElseThrow(() -> new FolioNotFoundException(stayPublicId));
		return view(folio, stay.publicId());
	}

	@Override
	public List<PaymentMethod> listPaymentMethods() {
		return payments.findAllMethods();
	}

	@Override
	public Optional<Folio> findFolioByStayId(Long stayId) {
		return folios.findByStayId(stayId);
	}

	private FolioView view(Folio folio, UUID stayPublicId) {
		var requests = new HashMap<Long, UUID>();
		folio.charges().stream().map(Charge::serviceRequestId).filter(Objects::nonNull).distinct()
				.forEach(id -> collaborators.findServiceRequest(id).ifPresent(r -> requests.put(id, r.publicId())));
		return new FolioView(folio, stayPublicId, requests);
	}

	private UUID stayPublicId(Folio folio) {
		return collaborators.findStay(folio.stayId()).map(StayCollaboratorSnapshot::publicId).orElseThrow();
	}
}
