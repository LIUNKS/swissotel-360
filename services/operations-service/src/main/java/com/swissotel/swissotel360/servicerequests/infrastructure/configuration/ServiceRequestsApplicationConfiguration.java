package com.swissotel.swissotel360.servicerequests.infrastructure.configuration;

import com.swissotel.swissotel360.servicerequests.application.commandservices.ServiceRequestCommandService;
import com.swissotel.swissotel360.servicerequests.application.internal.commandservices.ServiceRequestCommandServiceImpl;
import com.swissotel.swissotel360.servicerequests.application.internal.outboundservices.acl.ServiceRequestCollaboratorsOutboundService;
import com.swissotel.swissotel360.servicerequests.application.internal.queryservices.ServiceRequestQueryServiceImpl;
import com.swissotel.swissotel360.servicerequests.application.port.ServiceRequestEventPublisher;
import com.swissotel.swissotel360.servicerequests.application.queryservices.ServiceRequestQueryService;
import com.swissotel.swissotel360.servicerequests.domain.repositories.ServiceRequestRepository;
import com.swissotel.swissotel360.servicerequests.infrastructure.transaction.TransactionalServiceRequestCommandService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class ServiceRequestsApplicationConfiguration {
	@Bean
	public ServiceRequestCommandServiceImpl serviceRequestCommandServiceImpl(ServiceRequestRepository repository,
			ServiceRequestCollaboratorsOutboundService collaborators, ServiceRequestEventPublisher events) {
		return new ServiceRequestCommandServiceImpl(repository, collaborators, events,
				Clock.system(ZoneId.of("America/Lima")));
	}

	@Bean
	@Primary
	public ServiceRequestCommandService serviceRequestCommandService(ServiceRequestCommandServiceImpl implementation,
			PlatformTransactionManager manager) {
		return new TransactionalServiceRequestCommandService(implementation, new TransactionTemplate(manager));
	}

	@Bean
	public ServiceRequestQueryService serviceRequestQueryService(ServiceRequestRepository repository,
			ServiceRequestCollaboratorsOutboundService collaborators) {
		return new ServiceRequestQueryServiceImpl(repository, collaborators);
	}
}
