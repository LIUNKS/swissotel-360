package com.swissotel.swissotel360.reservations.application.port;

import com.swissotel.swissotel360.reservations.domain.model.ReservationCode;
import com.swissotel.swissotel360.reservations.domain.model.ReservationId;
import com.swissotel.swissotel360.shared.domain.model.valueobjects.Money;

public interface PrepaymentPort {
	PrepaymentRequest request(ReservationId id, ReservationCode code, Money amount);

	record PrepaymentRequest(String status, String message) {
		public static PrepaymentRequest unavailable() {
			return new PrepaymentRequest("not_configured", "Prepayment processing is not configured");
		}
	}
}
