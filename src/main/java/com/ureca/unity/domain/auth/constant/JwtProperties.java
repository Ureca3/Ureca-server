package com.ureca.unity.domain.auth.constant;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        String issuer,
        String secret,
        long accessExpirationSeconds,
        long refreshExpirationSeconds
) {}
