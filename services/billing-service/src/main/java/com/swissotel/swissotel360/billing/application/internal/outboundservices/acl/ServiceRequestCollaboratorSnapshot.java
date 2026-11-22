package com.swissotel.swissotel360.billing.application.internal.outboundservices.acl;

import java.util.UUID;

/** Read-only service request collaborator snapshot decoupled from service requests bounded context. */
public record ServiceRequestCollaboratorSnapshot(
		Long id,
		UUID publicId,
		Long stayId,
		String serviceTypeCode,
		String status) {
}
