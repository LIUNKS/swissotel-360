package com.swissotel.swissotel360.iam.application.internal.outboundservices.tokens;

import java.util.Collection;
import java.util.UUID;

public interface TokenService {
	String getUsernameFromToken(String token);

	String generateToken(Long userId, UUID publicId, String email, String role, Collection<String> permissions);

	boolean validateToken(String token);
}
