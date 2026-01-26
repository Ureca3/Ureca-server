package com.ureca.unity.domain.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class TokenResponse {
    private final String accessToken;
    private final long userId;
    private final long accessTokenExpiresIn;
}
