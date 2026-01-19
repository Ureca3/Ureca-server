package com.ureca.unity.domain.auth.service;

import com.ureca.unity.domain.auth.constant.OAuthProvider;
import com.ureca.unity.domain.auth.dto.OAuthLoginResult;

public interface OAuthService {

    OAuthLoginResult login(OAuthProvider provider, String authorizationCode);
}
