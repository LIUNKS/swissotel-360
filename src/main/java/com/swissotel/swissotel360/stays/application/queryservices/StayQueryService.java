package com.swissotel.swissotel360.stays.application.queryservices;

import com.swissotel.swissotel360.stays.application.dto.StayView;
import com.swissotel.swissotel360.stays.domain.aggregates.Stay;
import com.swissotel.swissotel360.stays.domain.model.StayStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StayQueryService {
	StayView getView(UUID stayPublicId);

	List<StayView> list(StayStatus status);

	Optional<Stay> findByPublicId(UUID stayPublicId);

	Optional<Stay> findById(Long id);
}
