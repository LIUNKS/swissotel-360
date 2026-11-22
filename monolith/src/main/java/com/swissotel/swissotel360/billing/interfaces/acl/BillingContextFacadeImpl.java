package com.swissotel.swissotel360.billing.interfaces.acl;

import com.swissotel.swissotel360.billing.application.queryservices.BillingQueryService;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
public class BillingContextFacadeImpl implements BillingContextFacade {
	private final BillingQueryService queries;

	public BillingContextFacadeImpl(BillingQueryService queries) {
		this.queries = queries;
	}

	@Override
	public Optional<FolioContextSnapshot> findFolioByStayId(Long stayId) {
		return queries.findFolioByStayId(stayId).map(f -> new FolioContextSnapshot(f.dbId(), f.id().value(), f.stayId(),
				f.status().code(), f.total()));
	}
}