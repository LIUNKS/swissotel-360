package com.swissotel.swissotel360.billing.domain.repositories;

import com.swissotel.swissotel360.billing.domain.aggregates.Folio;
import com.swissotel.swissotel360.billing.domain.model.FolioId;
import java.util.Optional;

public interface FolioRepository {
	/** Persists the folio and any new (dbId == null) charges. */
	Folio save(Folio folio);

	Optional<Folio> findByPublicId(FolioId id);

	Optional<Folio> findById(Long id);

	Optional<Folio> findByStayId(Long stayId);

	boolean existsChargeForServiceRequest(Long serviceRequestId);
}
