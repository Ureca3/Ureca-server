package com.ureca.unity.domain.auth.service;

import com.ureca.unity.domain.auth.dto.OAuthLoginResponse;
import com.ureca.unity.domain.auth.constant.OAuthProvider;
import com.ureca.unity.domain.auth.dto.OAuthUserInfo;

public interface OAuthService {

    OAuthLoginResponse login(OAuthProvider provider, OAuthUserInfo authorizationCode);
}
