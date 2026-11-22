package com.swissotel.swissotel360.billing.infrastructure.configuration;

import com.swissotel.swissotel360.billing.application.commandservices.BillingCommandService;
import com.swissotel.swissotel360.billing.application.internal.commandservices.BillingCommandServiceImpl;
import com.swissotel.swissotel360.billing.application.internal.outboundservices.acl.BillingCollaboratorsOutboundService;
import com.swissotel.swissotel360.billing.application.internal.queryservices.BillingQueryServiceImpl;
import com.swissotel.swissotel360.billing.application.queryservices.BillingQueryService;
import com.swissotel.swissotel360.billing.domain.repositories.FolioRepository;
import com.swissotel.swissotel360.billing.domain.repositories.InvoiceRepository;
import com.swissotel.swissotel360.billing.domain.repositories.PaymentRepository;
import com.swissotel.swissotel360.billing.infrastructure.transaction.TransactionalBillingCommandService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class BillingApplicationConfiguration {
	@Bean
	public BillingCommandServiceImpl billingCommandServiceImpl(FolioRepository folios, PaymentRepository payments,
			InvoiceRepository invoices, BillingCollaboratorsOutboundService collaborators) {
		return new BillingCommandServiceImpl(folios, payments, invoices, collaborators,
				Clock.system(ZoneId.of("America/Lima")));
	}

	@Bean
	@Primary
	public BillingCommandService billingCommandService(BillingCommandServiceImpl implementation,
			PlatformTransactionManager manager) {
		// REQUIRES_NEW: event handlers run in afterCommit of the publisher's (already committed) transaction.
		var transactions = new TransactionTemplate(manager);
		transactions.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
		return new TransactionalBillingCommandService(implementation, transactions);
	}

	@Bean
	public BillingQueryService billingQueryService(FolioRepository folios, PaymentRepository payments,
			BillingCollaboratorsOutboundService collaborators) {
		return new BillingQueryServiceImpl(folios, payments, collaborators);
	}
}