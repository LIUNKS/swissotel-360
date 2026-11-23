package com.swissotel.swissotel360.servicerequests.interfaces.acl;

import java.util.UUID;

/** status is "registered", "in_progress" or "completed". */
public record ServiceRequestContextSnapshot(Long id, UUID publicId, Long stayId, String serviceTypeCode,
		String status) {
}
