package com.example.evcharging.framework.security;

import java.time.Instant;

public record VerifiedAccessToken(
        AccessPrincipal principal,
        String tokenId,
        String sessionId,
        Instant expiresAt
) {}
