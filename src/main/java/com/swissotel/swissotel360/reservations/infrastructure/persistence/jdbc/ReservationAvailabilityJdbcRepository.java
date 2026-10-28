package com.swissotel.swissotel360.reservations.infrastructure.persistence.jdbc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.swissotel.swissotel360.reservations.domain.repositories.ReservationAvailabilityRepository;
import java.time.LocalDate;
import java.util.UUID;
@Repository public class ReservationAvailabilityJdbcRepository implements ReservationAvailabilityRepository {
    private final JdbcTemplate jdbcTemplate;
    public ReservationAvailabilityJdbcRepository(JdbcTemplate jdbcTemplate) { this.jdbcTemplate=jdbcTemplate; }
    @Override public long countActiveOverlapping(UUID roomTypePublicId,LocalDate checkIn,LocalDate checkOut) {
        Long count=jdbcTemplate.queryForObject("select count(*) from reservations r join room_types rt on rt.id=r.room_type_id where rt.public_id=? and r.status in ('pending','confirmed') and r.check_in_date < ? and r.check_out_date > ?",Long.class,roomTypePublicId,checkOut,checkIn);
        return count==null?0:count;
    }
    @Override public boolean hasReservations(UUID roomTypePublicId) { Boolean found=jdbcTemplate.queryForObject("select exists(select 1 from reservations r join room_types rt on rt.id=r.room_type_id where rt.public_id=?)",Boolean.class,roomTypePublicId); return Boolean.TRUE.equals(found); }
}
