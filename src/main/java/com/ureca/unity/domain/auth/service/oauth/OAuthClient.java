package com.ureca.unity.domain.auth.service.oauth;

import com.ureca.unity.domain.auth.dto.OAuthAuthResult;

public interface OAuthClient {
    OAuthAuthResult authenticate(String authorizationCode);
}
