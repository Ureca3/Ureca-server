package com.ureca.unity.domain.auth.service;

import com.ureca.unity.domain.auth.dto.OAuthLoginResponse;

public interface OAuthService {

    OAuthLoginResponse login(String provider, String authorizationCode);
}
