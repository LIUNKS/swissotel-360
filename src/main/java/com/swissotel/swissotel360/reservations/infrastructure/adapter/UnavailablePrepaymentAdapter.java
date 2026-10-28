package com.swissotel.swissotel360.reservations.infrastructure.adapter;

import com.swissotel.swissotel360.reservations.application.port.PrepaymentPort;
import com.swissotel.swissotel360.reservations.domain.model.ReservationCode;
import com.swissotel.swissotel360.reservations.domain.model.ReservationId;
import com.swissotel.swissotel360.shared.domain.model.valueobjects.Money;
import org.springframework.stereotype.Component;

@Component
public class UnavailablePrepaymentAdapter implements PrepaymentPort {
    @Override public PrepaymentRequest request(ReservationId id,ReservationCode code,Money amount) { return PrepaymentRequest.unavailable(); }
}
