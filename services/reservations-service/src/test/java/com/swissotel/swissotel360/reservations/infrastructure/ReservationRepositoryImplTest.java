package com.swissotel.swissotel360.reservations.infrastructure;

import com.swissotel.swissotel360.reservations.domain.model.*;
import com.swissotel.swissotel360.reservations.infrastructure.persistence.jpa.adapters.ReservationRepositoryImpl;
import com.swissotel.swissotel360.reservations.infrastructure.persistence.jpa.entities.ReservationPersistenceEntity;
import com.swissotel.swissotel360.reservations.infrastructure.persistence.jpa.repositories.ReservationPersistenceRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReservationRepositoryImplTest {
	private final ReservationPersistenceRepository persistence = mock(ReservationPersistenceRepository.class);
	private final ReservationRepositoryImpl repository = new ReservationRepositoryImpl(persistence);

	@Test
	@SuppressWarnings("unchecked")
	void searchWithNullFiltersCallsFindAllAndBuildsEmptyPredicate() {
		var entity = sampleEntity();
		when(persistence.findAll(any(Specification.class), any(Pageable.class)))
				.thenReturn(new PageImpl<>(List.of(entity), PageRequest.of(0, 20), 1L));

		var result = repository.search(new ReservationSearch(null, null, null, null, null, 0, 20));

		assertEquals(1L, result.totalElements());
		assertEquals(1, result.content().size());
		assertEquals(entity.getPublicId(), result.content().get(0).id().value());

		ArgumentCaptor<Specification<ReservationPersistenceEntity>> specCaptor = ArgumentCaptor
				.forClass(Specification.class);
		ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
		verify(persistence).findAll(specCaptor.capture(), pageableCaptor.capture());
		assertEquals(PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt")), pageableCaptor.getValue());

		Root<ReservationPersistenceEntity> root = mock(Root.class);
		CriteriaQuery<?> query = mock(CriteriaQuery.class);
		CriteriaBuilder cb = mock(CriteriaBuilder.class);
		Predicate combined = mock(Predicate.class);
		when(cb.and(any(Predicate[].class))).thenReturn(combined);

		Predicate actual = specCaptor.getValue().toPredicate(root, query, cb);

		assertSame(combined, actual);
		verifyNoInteractions(root);
		verify(cb).and(new Predicate[0]);
		verifyNoMoreInteractions(cb);
	}

	@Test
	@SuppressWarnings("unchecked")
	void searchWithPartialFiltersBuildsOnlyPresentPredicates() {
		when(persistence.findAll(any(Specification.class), any(Pageable.class)))
				.thenReturn(new PageImpl<>(List.of(), PageRequest.of(1, 10), 0L));

		var result = repository
				.search(new ReservationSearch(ReservationStatus.CONFIRMED, null, "  Sw26-Ab  ", null, null, 1, 10));

		assertEquals(0L, result.totalElements());
		assertTrue(result.content().isEmpty());

		ArgumentCaptor<Specification<ReservationPersistenceEntity>> specCaptor = ArgumentCaptor
				.forClass(Specification.class);
		ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
		verify(persistence).findAll(specCaptor.capture(), pageableCaptor.capture());
		assertEquals(PageRequest.of(1, 10, Sort.by(Sort.Direction.DESC, "createdAt")), pageableCaptor.getValue());

		Root<ReservationPersistenceEntity> root = mock(Root.class);
		CriteriaQuery<?> query = mock(CriteriaQuery.class);
		CriteriaBuilder cb = mock(CriteriaBuilder.class);
		Path<Object> statusPath = mock(Path.class);
		Path<String> codePath = mock(Path.class);
		Expression<String> lowerCodeExpr = mock(Expression.class);
		Predicate statusPred = mock(Predicate.class);
		Predicate codePred = mock(Predicate.class);
		Predicate combined = mock(Predicate.class);

		when(root.get("status")).thenReturn(statusPath);
		when(root.<String>get("reservationCode")).thenReturn(codePath);
		when(cb.equal(statusPath, "confirmed")).thenReturn(statusPred);
		when(cb.lower(codePath)).thenReturn(lowerCodeExpr);
		when(cb.like(lowerCodeExpr, "%sw26-ab%")).thenReturn(codePred);
		when(cb.and(any(Predicate[].class))).thenReturn(combined);

		Predicate actual = specCaptor.getValue().toPredicate(root, query, cb);

		assertSame(combined, actual);
		verify(root).get("status");
		verify(root).get("reservationCode");
		verifyNoMoreInteractions(root);
		verify(cb).equal(statusPath, "confirmed");
		verify(cb).lower(codePath);
		verify(cb).like(lowerCodeExpr, "%sw26-ab%");
		verify(cb).and(new Predicate[] { statusPred, codePred });
		verifyNoMoreInteractions(cb);
	}

	@Test
	@SuppressWarnings("unchecked")
	void searchWithAllFiltersBuildsAllPredicates() {
		var from = LocalDate.parse("2026-11-01");
		var to = LocalDate.parse("2026-11-15");
		when(persistence.findAll(any(Specification.class), any(Pageable.class)))
				.thenReturn(new PageImpl<>(List.of(), PageRequest.of(2, 5), 0L));

		repository.search(
				new ReservationSearch(ReservationStatus.PENDING, new GuestId(42L), "SW26-7H3K", from, to, 2, 5));

		ArgumentCaptor<Specification<ReservationPersistenceEntity>> specCaptor = ArgumentCaptor
				.forClass(Specification.class);
		ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
		verify(persistence).findAll(specCaptor.capture(), pageableCaptor.capture());
		assertEquals(PageRequest.of(2, 5, Sort.by(Sort.Direction.DESC, "createdAt")), pageableCaptor.getValue());

		Root<ReservationPersistenceEntity> root = mock(Root.class);
		CriteriaQuery<?> query = mock(CriteriaQuery.class);
		CriteriaBuilder cb = mock(CriteriaBuilder.class);
		Path<Object> statusPath = mock(Path.class);
		Path<Object> guestIdPath = mock(Path.class);
		Path<String> codePath = mock(Path.class);
		Path<LocalDate> checkInDatePath = mock(Path.class);
		Expression<String> lowerCodeExpr = mock(Expression.class);
		Predicate statusPred = mock(Predicate.class);
		Predicate guestPred = mock(Predicate.class);
		Predicate codePred = mock(Predicate.class);
		Predicate fromPred = mock(Predicate.class);
		Predicate toPred = mock(Predicate.class);
		Predicate combined = mock(Predicate.class);

		when(root.get("status")).thenReturn(statusPath);
		when(root.get("guestId")).thenReturn(guestIdPath);
		when(root.<String>get("reservationCode")).thenReturn(codePath);
		when(root.<LocalDate>get("checkInDate")).thenReturn(checkInDatePath);
		when(cb.equal(statusPath, "pending")).thenReturn(statusPred);
		when(cb.equal(guestIdPath, 42L)).thenReturn(guestPred);
		when(cb.lower(codePath)).thenReturn(lowerCodeExpr);
		when(cb.like(lowerCodeExpr, "%sw26-7h3k%")).thenReturn(codePred);
		when(cb.greaterThanOrEqualTo(checkInDatePath, from)).thenReturn(fromPred);
		when(cb.lessThanOrEqualTo(checkInDatePath, to)).thenReturn(toPred);
		when(cb.and(any(Predicate[].class))).thenReturn(combined);

		Predicate actual = specCaptor.getValue().toPredicate(root, query, cb);

		assertSame(combined, actual);
		verify(cb).equal(statusPath, "pending");
		verify(cb).equal(guestIdPath, 42L);
		verify(cb).lower(codePath);
		verify(cb).like(lowerCodeExpr, "%sw26-7h3k%");
		verify(cb).greaterThanOrEqualTo(checkInDatePath, from);
		verify(cb).lessThanOrEqualTo(checkInDatePath, to);
		verify(cb).and(new Predicate[] { statusPred, guestPred, codePred, fromPred, toPred });
		verifyNoMoreInteractions(cb);
	}

	@Test
	void countActiveOverlappingWithoutExcludingCallsNonExcludingQuery() {
		var typeId = new RoomTypeId(3L);
		var period = new StayPeriod(LocalDate.parse("2026-11-10"), LocalDate.parse("2026-11-13"));
		when(persistence.countActiveOverlapping(3L, period.checkIn(), period.checkOut())).thenReturn(2L);

		long count = repository.countActiveOverlapping(typeId, period, Optional.empty());

		assertEquals(2L, count);
		verify(persistence).countActiveOverlapping(3L, period.checkIn(), period.checkOut());
		verify(persistence, never()).countActiveOverlappingExcluding(any(), any(), any(), any());
	}

	@Test
	void countActiveOverlappingWithExcludingCallsExcludingQuery() {
		var typeId = new RoomTypeId(3L);
		var period = new StayPeriod(LocalDate.parse("2026-11-10"), LocalDate.parse("2026-11-13"));
		var excluding = new ReservationId(UUID.randomUUID());
		when(persistence.countActiveOverlappingExcluding(3L, period.checkIn(), period.checkOut(), excluding.value()))
				.thenReturn(1L);

		long count = repository.countActiveOverlapping(typeId, period, Optional.of(excluding));

		assertEquals(1L, count);
		verify(persistence).countActiveOverlappingExcluding(3L, period.checkIn(), period.checkOut(), excluding.value());
		verify(persistence, never()).countActiveOverlapping(any(), any(), any());
	}

	private static ReservationPersistenceEntity sampleEntity() {
		var entity = new ReservationPersistenceEntity();
		entity.setPublicId(UUID.randomUUID());
		entity.setReservationCode("SW26-7H3KQ9XA");
		entity.setGuestId(42L);
		entity.setRoomTypeId(3L);
		entity.setCheckInDate(LocalDate.parse("2026-11-10"));
		entity.setCheckOutDate(LocalDate.parse("2026-11-13"));
		entity.setStatus("confirmed");
		entity.setPaymentType("deferred");
		entity.setTotalAmount(new BigDecimal("1140.00"));
		entity.setCreatedBy(9L);
		entity.setCreatedAt(Instant.parse("2026-10-27T18:30:00Z"));
		return entity;
	}
}
