package com.ureca.unity.domain.auth.service.oauth;

import com.ureca.unity.domain.auth.constant.OAuthProvider;
import com.ureca.unity.domain.auth.dto.OAuthUserInfo;
import org.springframework.stereotype.Component;

@Component("google")
public class GoogleOAuthClient implements OAuthClient {

    @Override
    public OAuthProvider getProvider() {
        return OAuthProvider.GOOGLE;
    }

    @Override
    public OAuthUserInfo getUserInfo(String code) {
        return "";
    }
}
