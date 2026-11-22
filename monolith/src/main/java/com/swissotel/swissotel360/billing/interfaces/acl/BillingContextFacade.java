package com.swissotel.swissotel360.billing.interfaces.acl;

import java.util.Optional;

public interface BillingContextFacade {
	Optional<FolioContextSnapshot> findFolioByStayId(Long stayId);
}