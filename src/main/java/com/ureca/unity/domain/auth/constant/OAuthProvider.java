package com.ureca.unity.domain.auth.constant;

import java.util.Arrays;

public enum OAuthProvider {

    GOOGLE("google"),
    NAVER("naver"),
    KAKAO("kakao");

    private final String value;

    OAuthProvider(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static OAuthProvider from(String provider) {
        return Arrays.stream(values())
                .filter(p -> p.value.equalsIgnoreCase(provider))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("Unsupported OAuth provider: " + provider)
                );
    }
}
