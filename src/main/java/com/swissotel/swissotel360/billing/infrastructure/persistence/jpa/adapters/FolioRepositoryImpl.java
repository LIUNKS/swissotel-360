package com.swissotel.swissotel360.billing.infrastructure.persistence.jpa.adapters;

import com.swissotel.swissotel360.billing.domain.aggregates.Folio;
import com.swissotel.swissotel360.billing.domain.model.FolioId;
import com.swissotel.swissotel360.billing.domain.repositories.FolioRepository;
import com.swissotel.swissotel360.billing.infrastructure.persistence.jpa.assemblers.BillingPersistenceAssembler;
import com.swissotel.swissotel360.billing.infrastructure.persistence.jpa.entities.FolioPersistenceEntity;
import com.swissotel.swissotel360.billing.infrastructure.persistence.jpa.repositories.ChargePersistenceRepository;
import com.swissotel.swissotel360.billing.infrastructure.persistence.jpa.repositories.FolioPersistenceRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public class FolioRepositoryImpl implements FolioRepository {
	private final FolioPersistenceRepository folios;
	private final ChargePersistenceRepository charges;

	public FolioRepositoryImpl(FolioPersistenceRepository folios, ChargePersistenceRepository charges) {
		this.folios = folios;
		this.charges = charges;
	}

	@Override
	public Folio save(Folio folio) {
		var entity = folio.dbId() == null ? new FolioPersistenceEntity() : folios.findById(folio.dbId()).orElseThrow();
		var saved = folios.saveAndFlush(BillingPersistenceAssembler.toEntity(folio, entity));
		folio.charges().stream().filter(c -> c.dbId() == null)
				.forEach(c -> charges.save(BillingPersistenceAssembler.toEntity(c, saved.getId())));
		charges.flush();
		return toDomain(saved);
	}

	@Override
	public Optional<Folio> findByPublicId(FolioId id) {
		return folios.findByPublicId(id.value()).map(this::toDomain);
	}

	@Override
	public Optional<Folio> findById(Long id) {
		return folios.findById(id).map(this::toDomain);
	}

	@Override
	public Optional<Folio> findByStayId(Long stayId) {
		return folios.findByStayId(stayId).map(this::toDomain);
	}

	@Override
	public boolean existsChargeForServiceRequest(Long serviceRequestId) {
		return charges.existsByServiceRequestId(serviceRequestId);
	}

	private Folio toDomain(FolioPersistenceEntity e) {
		return BillingPersistenceAssembler.toDomain(e, charges.findByFolioIdOrderByIdAsc(e.getId()));
	}
}