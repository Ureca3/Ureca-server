package com.ureca.unity.domain.auth.service.oauth;

import com.ureca.unity.domain.auth.dto.OAuthAuthResult;
import com.ureca.unity.domain.auth.dto.OAuthTokenInfo;
import com.ureca.unity.domain.auth.dto.OAuthUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component("naver")
@RequiredArgsConstructor
public class NaverOAuthClient implements OAuthClient {

    @Value("${oauth.naver.client-id}")
    private String clientId;

    @Value("${oauth.naver.client-secret}")
    private String clientSecret;

    @Value("${oauth.naver.token-uri}")
    private String tokenUri;

    @Value("${oauth.naver.user-info-uri}")
    private String userInfoUri;

    @Value("${oauth.naver.redirect-uri}")
    private String redirectUri;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public OAuthAuthResult authenticate(String authorizationCode) {
        Map<String, Object> token = getTokenResponse(authorizationCode);

        String accessToken = token.get("access_token").toString();
        String refreshToken = token.get("refresh_token") != null ? String.valueOf(token.get("refresh_token")) : null;
        Long expiresIn = token.get("expires_in") != null ? Long.valueOf(String.valueOf(token.get("expires_in"))) : null;

        OAuthUserInfo userInfo = fetchUserInfo(accessToken);

        return new OAuthAuthResult(
                userInfo,
                new OAuthTokenInfo(accessToken, refreshToken, expiresIn)
        );
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Map<String, Object> getTokenResponse(String code) {

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
            throw new IllegalArgumentException("Failed to retrieve Naver access token");
        }

        return (Map<String, Object>) response.getBody();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
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

        if (response.getBody() == null || response.getBody().get("response") == null) {
            throw new IllegalArgumentException("Failed to retrieve Naver user info");
        }

        Map<String, Object> responseBody =
                (Map<String, Object>) response.getBody().get("response");

        String name = (String) responseBody.get("name");
        if (name == null || name.isBlank()) {
            name = "naver_user";
        }

        return OAuthUserInfo.builder()
                .provider("naver")
                .providerId(responseBody.get("id").toString())
                .email((String) responseBody.get("email"))   // nullable
                .name(name)
                .build();
    }
}
