package com.ureca.unity.domain.auth.service;

import com.ureca.unity.domain.auth.constant.OAuthProvider;
import com.ureca.unity.domain.auth.dto.OAuthLoginResponse;
import org.springframework.stereotype.Service;

@Service
public class OAuthServiceImpl implements OAuthService {

    @Override
    public OAuthLoginResponse login(OAuthProvider provider, String authorizationCode) {
        throw new UnsupportedOperationException("OAuthService not implemented yet");
    }
}
