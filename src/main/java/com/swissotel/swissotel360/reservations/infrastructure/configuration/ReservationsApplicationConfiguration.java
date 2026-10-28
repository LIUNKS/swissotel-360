package com.swissotel.swissotel360.reservations.infrastructure.configuration;
import com.swissotel.swissotel360.reservations.application.internal.queryservices.ReservationsContextFacadeImpl;
import com.swissotel.swissotel360.reservations.domain.repositories.ReservationAvailabilityRepository;
import com.swissotel.swissotel360.reservations.interfaces.acl.ReservationsContextFacade;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration public class ReservationsApplicationConfiguration {
    @Bean public ReservationsContextFacade reservationsContextFacade(ReservationAvailabilityRepository repository) { return new ReservationsContextFacadeImpl(repository); }
}
