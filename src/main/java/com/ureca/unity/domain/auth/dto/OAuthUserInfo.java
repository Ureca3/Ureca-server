package com.ureca.unity.domain.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class OAuthUserInfo {

    private final String provider;
    private final String providerId;
    private final String email;
    private final String name;
}
