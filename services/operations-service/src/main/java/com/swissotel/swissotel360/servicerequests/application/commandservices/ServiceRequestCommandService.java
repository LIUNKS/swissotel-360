package com.swissotel.swissotel360.servicerequests.application.commandservices;

import com.swissotel.swissotel360.servicerequests.application.command.*;
import com.swissotel.swissotel360.servicerequests.domain.aggregates.ServiceRequest;

public interface ServiceRequestCommandService {
	ServiceRequest create(CreateServiceRequestCommand command);

	ServiceRequest updateStatus(UpdateServiceRequestStatusCommand command);
}
