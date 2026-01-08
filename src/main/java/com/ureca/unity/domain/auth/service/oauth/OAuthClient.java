package com.ureca.unity.domain.auth.service.oauth;

import com.ureca.unity.domain.auth.dto.OAuthUserInfo;

/**
 * OAuth 제공자(Google, Naver, Kakao)와의 통신을 담당하는 Client 인터페이스
 */
public interface OAuthClient {

    /**
     * Authorization Code를 받아 OAuth 사용자 정보를 조회한다.
     */
    OAuthUserInfo getUserInfo(String authorizationCode);
}
