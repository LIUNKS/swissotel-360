package com.swissotel.swissotel360.rooms.application.queryservices;
import com.swissotel.swissotel360.rooms.domain.model.aggregates.Rate;
import java.util.List;
import com.swissotel.swissotel360.rooms.domain.model.queries.GetRatesByRoomTypeQuery;
public interface RateQueryService { List<Rate> findByRoomType(GetRatesByRoomTypeQuery query); }
