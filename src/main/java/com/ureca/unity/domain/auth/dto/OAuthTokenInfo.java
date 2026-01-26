package com.ureca.unity.domain.auth.dto;

public record OAuthTokenInfo(
        String accessToken,
        String refreshToken,      // nullable
        Long expiresInSeconds     // nullable
) {}
