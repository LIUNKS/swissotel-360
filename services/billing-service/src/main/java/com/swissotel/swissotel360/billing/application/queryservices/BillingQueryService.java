package com.swissotel.swissotel360.billing.application.queryservices;

import com.swissotel.swissotel360.billing.application.dto.FolioView;
import com.swissotel.swissotel360.billing.domain.aggregates.Folio;
import com.swissotel.swissotel360.billing.domain.model.PaymentMethod;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BillingQueryService {
	FolioView getFolio(UUID folioId);

	FolioView getFolioByStay(UUID stayPublicId);

	List<PaymentMethod> listPaymentMethods();

	Optional<Folio> findFolioByStayId(Long stayId);
}