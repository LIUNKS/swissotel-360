package com.swissotel.swissotel360.iam.application.internal.outboundservices.tokens;

public interface TokenService {
    String getUsernameFromToken(String token);

    String generateToken(String username);

    boolean validateToken(String token);
}
