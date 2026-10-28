package com.swissotel.swissotel360.reservations.infrastructure.persistence.jdbc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.swissotel.swissotel360.reservations.domain.repositories.ReservationAvailabilityRepository;
import java.time.LocalDate;
@Repository public class ReservationAvailabilityJdbcRepository implements ReservationAvailabilityRepository {
    private final JdbcTemplate jdbcTemplate;
    public ReservationAvailabilityJdbcRepository(JdbcTemplate jdbcTemplate) { this.jdbcTemplate=jdbcTemplate; }
    @Override public long countActiveOverlapping(Long roomTypeId,LocalDate checkIn,LocalDate checkOut) {
        Long count=jdbcTemplate.queryForObject("select count(*) from reservations r where r.room_type_id=? and r.status in ('pending','confirmed') and r.check_in_date < ? and r.check_out_date > ?",Long.class,roomTypeId,checkOut,checkIn);
        return count==null?0:count;
    }
    @Override public boolean hasReservations(Long roomTypeId) { Boolean found=jdbcTemplate.queryForObject("select exists(select 1 from reservations r where r.room_type_id=?)",Boolean.class,roomTypeId); return Boolean.TRUE.equals(found); }
}
