package com.swissotel.swissotel360.billing.interfaces.acl;

import java.math.BigDecimal;
import java.util.UUID;

/** status is "open" or "closed". */
public record FolioContextSnapshot(Long id, UUID publicId, Long stayId, String status, BigDecimal totalAmount) {
}