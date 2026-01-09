package com.ureca.unity.domain.auth.service.oauth;

import com.ureca.unity.domain.auth.constant.OAuthProvider;
import com.ureca.unity.domain.auth.dto.OAuthUserInfo;
import org.springframework.stereotype.Component;

@Component("naver")
public class NaverOAuthClient implements OAuthClient {

    @Override
    public OAuthProvider getProvider() {
        return OAuthProvider.NAVER;
    }

    @Override
    public OAuthUserInfo getUserInfo(String code) {
        return "";
    }
}
