package com.swissotel.swissotel360.reservations.application.queryservices;

import com.swissotel.swissotel360.reservations.application.dto.*;
import com.swissotel.swissotel360.reservations.application.query.*;
import com.swissotel.swissotel360.reservations.application.port.CurrentActorPort.CurrentActor;
public interface ReservationQueryService {
    ReservationResult findByPublicId(GetReservationQuery query, CurrentActor actor);
    ReservationResult findByCode(GetReservationByCodeQuery query, CurrentActor actor);
    ReservationSummaryPage search(SearchReservationsQuery query, CurrentActor actor);
}
