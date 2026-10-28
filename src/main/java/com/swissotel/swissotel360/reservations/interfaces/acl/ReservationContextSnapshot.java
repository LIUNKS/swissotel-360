package com.swissotel.swissotel360.reservations.interfaces.acl;

import java.time.LocalDate;
import java.util.UUID;
public record ReservationContextSnapshot(UUID publicId,String code,Long guestId,Long roomTypeId,LocalDate checkIn,LocalDate checkOut,String status) { }
