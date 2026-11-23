package com.swissotel.swissotel360.rooms.infrastructure.configuration;

import com.swissotel.swissotel360.rooms.application.commandservices.*;
import com.swissotel.swissotel360.rooms.application.internal.commandservices.*;
import com.swissotel.swissotel360.rooms.application.internal.queryservices.*;
import com.swissotel.swissotel360.rooms.application.queryservices.*;
import com.swissotel.swissotel360.rooms.domain.repositories.*;
import com.swissotel.swissotel360.rooms.application.internal.outboundservices.acl.ReservationsAvailabilityService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RoomsApplicationConfiguration {
	@Bean
	public RoomTypeCommandService roomTypeCommandService(RoomTypeRepository types, RoomRepository rooms,
			RateRepository rates, ReservationsAvailabilityService reservations) {
		return new RoomTypeCommandServiceImpl(types, rooms, rates, reservations);
	}

	@Bean
	public RoomTypeQueryService roomTypeQueryService(RoomTypeRepository types, RateRepository rates) {
		return new RoomTypeQueryServiceImpl(types, rates);
	}

	@Bean
	public RoomCommandService roomCommandService(RoomRepository rooms, RoomTypeRepository types) {
		return new RoomCommandServiceImpl(rooms, types);
	}

	@Bean
	public RoomQueryService roomQueryService(RoomRepository rooms, RoomTypeRepository types) {
		return new RoomQueryServiceImpl(rooms, types);
	}

	@Bean
	public RateCommandService rateCommandService(RateRepository rates, RoomTypeRepository types) {
		return new RateCommandServiceImpl(rates, types);
	}

	@Bean
	public RateQueryService rateQueryService(RateRepository rates, RoomTypeRepository types) {
		return new RateQueryServiceImpl(rates, types);
	}

	@Bean
	public AvailabilityQueryService availabilityQueryService(RoomTypeRepository types, RoomRepository rooms,
			RateRepository rates, ReservationsAvailabilityService reservations) {
		return new AvailabilityQueryServiceImpl(types, rooms, rates, reservations);
	}
}
