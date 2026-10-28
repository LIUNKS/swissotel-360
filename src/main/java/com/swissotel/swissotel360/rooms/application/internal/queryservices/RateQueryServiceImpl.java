package com.swissotel.swissotel360.rooms.application.internal.queryservices;
import com.swissotel.swissotel360.rooms.application.queryservices.RateQueryService;
import com.swissotel.swissotel360.rooms.domain.exceptions.RoomTypeNotFoundException;
import com.swissotel.swissotel360.rooms.domain.repositories.RateRepository;
import com.swissotel.swissotel360.rooms.domain.repositories.RoomTypeRepository;
import com.swissotel.swissotel360.rooms.domain.model.queries.GetRatesByRoomTypeQuery;
import java.util.UUID;
public class RateQueryServiceImpl implements RateQueryService {
    private final RateRepository rates; private final RoomTypeRepository roomTypes;
    public RateQueryServiceImpl(RateRepository rates, RoomTypeRepository roomTypes) { this.rates=rates; this.roomTypes=roomTypes; }
    public java.util.List<com.swissotel.swissotel360.rooms.domain.model.aggregates.Rate> findByRoomType(GetRatesByRoomTypeQuery query) { var id=query.roomTypePublicId(); var type=roomTypes.findByPublicId(id).orElseThrow(()->new RoomTypeNotFoundException(id)); return rates.findByRoomTypeId(type.id()); }
}
