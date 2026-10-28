package com.swissotel.swissotel360.reservations.application.port;

import java.util.Collection;
public interface ReservationEventPublisher { void publishAfterCommit(Collection<Object> events); }
