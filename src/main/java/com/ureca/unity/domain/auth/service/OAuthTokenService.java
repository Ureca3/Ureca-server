package com.ureca.unity.domain.auth.service;

import com.ureca.unity.domain.auth.model.OAuthToken;

import java.util.Optional;

public interface OAuthTokenService {
    void upsert(OAuthToken token);
    Optional<OAuthToken> find(Long userId, String provider);
    void deleteByUserId(Long userId);
}
