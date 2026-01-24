package com.ureca.unity.domain.auth.dto;

public record OAuthAuthResult(
        OAuthUserInfo userInfo,
        OAuthTokenInfo tokenInfo
) {}
