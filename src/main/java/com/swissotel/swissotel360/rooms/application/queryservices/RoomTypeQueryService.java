package com.swissotel.swissotel360.rooms.application.queryservices;
import java.util.List;
import com.swissotel.swissotel360.rooms.domain.model.queries.GetRoomTypeByPublicIdQuery;
public interface RoomTypeQueryService { List<RoomTypeView> findAll(); RoomTypeView findByPublicId(GetRoomTypeByPublicIdQuery query); }
