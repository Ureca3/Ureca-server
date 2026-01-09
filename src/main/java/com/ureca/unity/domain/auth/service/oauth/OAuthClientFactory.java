package com.ureca.unity.domain.auth.service.oauth;

import com.ureca.unity.domain.auth.constant.OAuthProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class OAuthClientFactory {

    private final Map<OAuthProvider, OAuthClient> clientMap;

    public OAuthClientFactory(List<OAuthClient> clients) {
        this.clientMap = new EnumMap<>(OAuthProvider.class);
        for (OAuthClient client : clients) {
            clientMap.put(client.getProvider(), client);
        }
    }

    public OAuthClient get(OAuthProvider provider) {
        OAuthClient client = clientMap.get(provider);
        if (client == null) {
            throw new IllegalArgumentException("지원하지 않는 OAuth Provider: " + provider);
        }
        return client;
    }
}