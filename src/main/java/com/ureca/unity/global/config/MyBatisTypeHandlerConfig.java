package com.ureca.unity.global.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class MyBatisTypeHandlerConfig {

    private final OAuthTokenCrypto crypto;

    @PostConstruct
    public void init() {
        EncryptedStringTypeHandler.setCrypto(crypto);
    }
}
