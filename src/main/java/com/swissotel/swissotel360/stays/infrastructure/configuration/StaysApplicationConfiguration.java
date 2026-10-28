package com.swissotel.swissotel360.stays.infrastructure.configuration;

import com.swissotel.swissotel360.stays.application.commandservices.StayCommandService;
import com.swissotel.swissotel360.stays.application.internal.commandservices.StayCommandServiceImpl;
import com.swissotel.swissotel360.stays.application.internal.outboundservices.acl.StayCollaboratorsOutboundService;
import com.swissotel.swissotel360.stays.application.internal.queryservices.StayQueryServiceImpl;
import com.swissotel.swissotel360.stays.application.port.StayEventPublisher;
import com.swissotel.swissotel360.stays.application.queryservices.StayQueryService;
import com.swissotel.swissotel360.stays.domain.repositories.StayRepository;
import com.swissotel.swissotel360.stays.infrastructure.transaction.TransactionalStayCommandService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class StaysApplicationConfiguration {
	@Bean
	public StayCommandServiceImpl stayCommandServiceImpl(StayRepository repository,
			StayCollaboratorsOutboundService collaborators, StayEventPublisher events) {
		return new StayCommandServiceImpl(repository, collaborators, events, Clock.system(ZoneId.of("America/Lima")));
	}

	@Bean
	@Primary
	public StayCommandService stayCommandService(StayCommandServiceImpl implementation,
			PlatformTransactionManager manager) {
		return new TransactionalStayCommandService(implementation, new TransactionTemplate(manager));
	}

	@Bean
	public StayQueryService stayQueryService(StayRepository repository,
			StayCollaboratorsOutboundService collaborators) {
		return new StayQueryServiceImpl(repository, collaborators);
	}
}
