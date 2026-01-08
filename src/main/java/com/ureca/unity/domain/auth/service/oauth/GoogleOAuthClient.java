package com.ureca.unity.domain.auth.service.oauth;

import com.ureca.unity.domain.auth.dto.OAuthUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component("google")
@RequiredArgsConstructor
public class GoogleOAuthClient implements OAuthClient {

    @Value("${oauth.google.client-id}")
    private String clientId;

    @Value("${oauth.google.client-secret}")
    private String clientSecret;

    @Value("${oauth.google.token-uri}")
    private String tokenUri;

    @Value("${oauth.google.user-info-uri}")
    private String userInfoUri;

    @Value("${oauth.google.redirect-uri}")
    private String redirectUri;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public OAuthUserInfo getUserInfo(String authorizationCode) {
        String accessToken = getAccessToken(authorizationCode);
        return fetchUserInfo(accessToken);
    }

    private String getAccessToken(String code) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("redirect_uri", redirectUri);
        body.add("code", code);

        HttpEntity<MultiValueMap<String, String>> request =
                new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                tokenUri,
                HttpMethod.POST,
                request,
                Map.class
        );

        if (response.getBody() == null || response.getBody().get("access_token") == null) {
            throw new IllegalArgumentException("Failed to retrieve Google access token");
        }

        return response.getBody().get("access_token").toString();
    }

    private OAuthUserInfo fetchUserInfo(String accessToken) {

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                userInfoUri,
                HttpMethod.GET,
                request,
                Map.class
        );

        if (response.getBody() == null || response.getBody().get("id") == null) {
            throw new IllegalArgumentException("Failed to retrieve Google user info");
        }

        Map<String, Object> body = response.getBody();

        String name = (String) body.get("name");
        if (name == null || name.isBlank()) {
            name = "google_user";
        }

        return OAuthUserInfo.builder()
                .provider("google")
                .providerId(body.get("id").toString())
                .email((String) body.get("email"))   // nullable
                .name((String) body.get("name"))
                .build();
    }
}