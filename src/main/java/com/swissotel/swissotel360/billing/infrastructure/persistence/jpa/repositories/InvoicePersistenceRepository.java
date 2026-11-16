package com.swissotel.swissotel360.billing.infrastructure.persistence.jpa.repositories;

import com.swissotel.swissotel360.billing.infrastructure.persistence.jpa.entities.InvoicePersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface InvoicePersistenceRepository extends JpaRepository<InvoicePersistenceEntity, Long> {
	boolean existsByPaymentId(Long paymentId);

	@Query("select max(i.number) from InvoicePersistenceEntity i where i.series = :series")
	Optional<String> findLastNumber(@Param("series") String series);
}