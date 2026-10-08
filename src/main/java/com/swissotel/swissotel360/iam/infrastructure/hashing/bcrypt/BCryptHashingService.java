package com.swissotel.swissotel360.iam.infrastructure.hashing.bcrypt;

import com.swissotel.swissotel360.iam.application.internal.outboundservices.hashing.HashingService;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Marker interface for the BCrypt hashing service.
 * Combines {@link HashingService} and Spring Security's {@link PasswordEncoder}.
 */
public interface BCryptHashingService extends HashingService, PasswordEncoder {
}
