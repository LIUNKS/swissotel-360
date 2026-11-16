package com.swissotel.swissotel360.rooms.application.queryservices;

import com.swissotel.swissotel360.rooms.domain.model.queries.RoomFilter;
import com.swissotel.swissotel360.rooms.domain.model.queries.GetRoomByPublicIdQuery;

public interface RoomQueryService {
	RoomPageView find(RoomFilter filter);

	RoomView findByPublicId(GetRoomByPublicIdQuery query);

	java.util.Optional<RoomView> findById(Long id);
}
