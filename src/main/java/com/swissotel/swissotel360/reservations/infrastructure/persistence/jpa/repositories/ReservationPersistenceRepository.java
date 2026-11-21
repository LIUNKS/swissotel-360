package com.swissotel.swissotel360.reservations.infrastructure.persistence.jpa.repositories;

import com.swissotel.swissotel360.reservations.infrastructure.persistence.jpa.entities.ReservationPersistenceEntity;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReservationPersistenceRepository
		extends JpaRepository<ReservationPersistenceEntity, Long>,
		JpaSpecificationExecutor<ReservationPersistenceEntity> {
	Optional<ReservationPersistenceEntity> findByPublicId(UUID publicId);

	Optional<ReservationPersistenceEntity> findByReservationCode(String code);

	boolean existsByReservationCode(String code);

	@Query("select count(r) from ReservationPersistenceEntity r where r.roomTypeId=:typeId and r.status in ('pending','confirmed') and r.checkInDate<:checkOut and r.checkOutDate>:checkIn")
	long countActiveOverlapping(@Param("typeId") Long typeId, @Param("checkIn") LocalDate checkIn,
			@Param("checkOut") LocalDate checkOut);

	@Query("select count(r) from ReservationPersistenceEntity r where r.roomTypeId=:typeId and r.status in ('pending','confirmed') and r.checkInDate<:checkOut and r.checkOutDate>:checkIn and r.publicId<>:excluding")
	long countActiveOverlappingExcluding(@Param("typeId") Long typeId, @Param("checkIn") LocalDate checkIn,
			@Param("checkOut") LocalDate checkOut, @Param("excluding") UUID excluding);

	@Query("select r from ReservationPersistenceEntity r where r.status='pending' and r.paymentType=:paymentType and r.createdAt<:threshold order by r.createdAt asc")
	List<ReservationPersistenceEntity> findExpired(@Param("threshold") Instant threshold,
			@Param("paymentType") String paymentType, Pageable pageable);
}
