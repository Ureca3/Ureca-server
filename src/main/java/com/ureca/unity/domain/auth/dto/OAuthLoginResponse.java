package com.ureca.unity.domain.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OAuthLoginResponse {

    private TokenResponse token;
    private boolean isRegistered; // 추가 정보 입력 필요 여부
}
