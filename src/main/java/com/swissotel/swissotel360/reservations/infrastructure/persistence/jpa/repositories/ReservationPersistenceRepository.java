package com.swissotel.swissotel360.reservations.infrastructure.persistence.jpa.repositories;

import com.swissotel.swissotel360.reservations.infrastructure.persistence.jpa.entities.ReservationPersistenceEntity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReservationPersistenceRepository extends JpaRepository<ReservationPersistenceEntity,Long> {
    Optional<ReservationPersistenceEntity> findByPublicId(UUID publicId);
    Optional<ReservationPersistenceEntity> findByReservationCode(String code);
    boolean existsByReservationCode(String code);
    @Query("select count(r) from ReservationPersistenceEntity r where r.roomTypeId=:typeId and r.status in ('pending','confirmed') and r.checkInDate<:checkOut and r.checkOutDate>:checkIn and (:excluding is null or r.publicId<>:excluding)")
    long countActiveOverlapping(@Param("typeId") Long typeId,@Param("checkIn") LocalDate checkIn,@Param("checkOut") LocalDate checkOut,@Param("excluding") UUID excluding);
    @Query("select r from ReservationPersistenceEntity r where r.status='pending' and r.paymentType=:paymentType and r.createdAt<:threshold order by r.createdAt asc")
    List<ReservationPersistenceEntity> findExpired(@Param("threshold") Instant threshold,@Param("paymentType") String paymentType,Pageable pageable);
    @Query("select r from ReservationPersistenceEntity r where (:status is null or r.status=:status) and (:guestId is null or r.guestId=:guestId) and (:code is null or lower(r.reservationCode) like lower(concat('%',:code,'%'))) and (:fromDate is null or r.checkInDate>=:fromDate) and (:toDate is null or r.checkInDate<=:toDate)")
    Page<ReservationPersistenceEntity> search(@Param("status") String status,@Param("guestId") Long guestId,@Param("code") String code,
            @Param("fromDate") LocalDate fromDate,@Param("toDate") LocalDate toDate,Pageable pageable);
}
