package com.ureca.unity.domain.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class OAuthLoginResponse {

    private final TokenResponse token;
    private final boolean requiresOnboarding;
}
