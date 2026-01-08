package com.ureca.unity.domain.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class OAuthUserInfo {

    private final String provider;     // google / naver / kakao
    private final String providerId;   // OAuth 제공자 고유 ID
    private final String email;        // nullable 가능
    private final String name;
}
