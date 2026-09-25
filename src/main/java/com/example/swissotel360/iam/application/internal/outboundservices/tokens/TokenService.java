package com.example.swissotel360.iam.application.internal.outboundservices.tokens;

/**
 * Outbound port for bearer token issuance and validation used by IAM commands and queries.
 */
public interface TokenService {

    /**
     * Extracts the username from a token.
     *
     * @param token token value
     * @return username embedded in the token
     */
    String getUsernameFromToken(String token);

    /**
     * Extracts the active session ID from a token.
     *
     * @param token token value
     * @return active session ID embedded in the token
     */
    String getActiveSessionIdFromToken(String token);

    /**
     * Generates a token for a username with an active session ID.
     *
     * @param username principal username
     * @param activeSessionId the active session ID
     * @return signed token value
     */
    String generateToken(String username, String activeSessionId);

    /**
     * Validates a token.
     *
     * @param token token value
     * @return {@code true} when token is valid; otherwise {@code false}
     */
    boolean validateToken(String token);
}
