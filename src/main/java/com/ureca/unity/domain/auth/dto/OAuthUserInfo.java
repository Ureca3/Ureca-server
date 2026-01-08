package com.ureca.unity.domain.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OAuthUserInfo {

    private String provider;     // google / naver / kakao
    private String providerId;   // OAuth provider unique id

    private String email;        // nullable 가능
    private String name;         // 사용자 이름
}
