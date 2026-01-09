package com.ureca.unity.domain.auth.service.oauth;

import com.ureca.unity.domain.auth.constant.OAuthProvider;
import com.ureca.unity.domain.auth.dto.OAuthUserInfo;

public interface OAuthClient {
    OAuthProvider getProvider();
    OAuthUserInfo getUserInfo(String code);
}