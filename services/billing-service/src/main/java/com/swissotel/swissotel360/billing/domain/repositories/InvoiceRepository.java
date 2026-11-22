package com.swissotel.swissotel360.billing.domain.repositories;

import com.swissotel.swissotel360.billing.domain.aggregates.Invoice;
import java.util.Optional;

public interface InvoiceRepository {
	Invoice save(Invoice invoice);

	boolean existsByPaymentId(Long paymentId);

	/** Highest issued number of a series, if any. */
	Optional<String> findLastNumber(String series);
}
