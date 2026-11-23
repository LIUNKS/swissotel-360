package com.swissotel.swissotel360.reservations.infrastructure.configuration;

import com.swissotel.swissotel360.reservations.application.commandservices.ReservationCommandService;
import com.swissotel.swissotel360.reservations.application.facade.ReservationFacade;
import com.swissotel.swissotel360.reservations.application.internal.commandservices.ReservationCommandServiceImpl;
import com.swissotel.swissotel360.reservations.application.internal.queryservices.ReservationQueryServiceImpl;
import com.swissotel.swissotel360.reservations.application.internal.facade.ReservationsContextFacadeImpl;
import com.swissotel.swissotel360.reservations.application.policy.ReservationAccessPolicy;
import com.swissotel.swissotel360.reservations.application.port.*;
import com.swissotel.swissotel360.reservations.application.queryservices.ReservationQueryService;
import com.swissotel.swissotel360.reservations.domain.repositories.ReservationRepository;
import com.swissotel.swissotel360.reservations.domain.services.*;
import com.swissotel.swissotel360.reservations.infrastructure.transaction.TransactionalReservationCommandService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import java.time.Clock;
import java.time.Duration;
import java.time.ZoneId;

@Configuration
@EnableScheduling
@EnableConfigurationProperties(ReservationProperties.class)
public class ReservationsApplicationConfiguration {
	@Bean
	public Clock reservationClock() {
		return Clock.system(ZoneId.of("America/Lima"));
	}

	@Bean
	public TransactionTemplate reservationTransactionTemplate(PlatformTransactionManager manager) {
		return new TransactionTemplate(manager);
	}

	@Bean
	public BookingPolicy bookingPolicy(ReservationProperties p) {
		return new BookingPolicy(p.maxNights());
	}

	@Bean
	public AvailabilityPolicy availabilityPolicy() {
		return new AvailabilityPolicy();
	}

	@Bean
	public ReservationAccessPolicy reservationAccessPolicy() {
		return new ReservationAccessPolicy();
	}

	@Bean
	public ReservationCommandServiceImpl reservationCommandServiceImpl(CurrentActorPort actors,
			GuestDirectoryPort guests,
			RoomInventoryPort rooms, PrepaymentPort payments, ReservationRepository repository,
			ReservationCodeGenerator codes,
			BookingPolicy booking, AvailabilityPolicy availability, ReservationEventPublisher events,
			ReservationAccessPolicy access,
			Clock reservationClock, ReservationProperties properties) {
		return new ReservationCommandServiceImpl(actors, guests, rooms, payments, repository, codes, booking,
				availability, events, access,
				reservationClock, Duration.ofMinutes(properties.pendingHoldMinutes()));
	}

	@Bean
	@Primary
	public ReservationCommandService reservationCommandService(ReservationCommandServiceImpl implementation,
			TransactionTemplate tx) {
		return new TransactionalReservationCommandService(implementation, tx);
	}

	@Bean
	public ReservationQueryService reservationQueryService(ReservationRepository repository, GuestDirectoryPort guests,
			RoomInventoryPort rooms, ReservationAccessPolicy access, ReservationProperties properties) {
		return new ReservationQueryServiceImpl(repository, guests, rooms, access,
				Duration.ofMinutes(properties.pendingHoldMinutes()));
	}

	@Bean
	public ReservationFacade reservationFacade(ReservationRepository repository, ReservationCommandService commands) {
		return new ReservationsContextFacadeImpl(repository, commands);
	}
}
