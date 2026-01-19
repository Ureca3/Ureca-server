package com.ureca.unity.domain.auth.dto;

public record OAuthLoginResult(
        OAuthLoginResponse response,
        String refreshToken
) {}
