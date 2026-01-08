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

@Component("kakao")
@RequiredArgsConstructor
public class KakaoOAuthClient implements OAuthClient {

    @Value("${oauth.kakao.client-id}")
    private String clientId;

    // Client Secret 안 쓰는 경우면 application-local.yml에도 없어야 함
    @Value("${oauth.kakao.client-secret:}")
    private String clientSecret;

    @Value("${oauth.kakao.token-uri}")
    private String tokenUri;

    @Value("${oauth.kakao.user-info-uri}")
    private String userInfoUri;

    @Value("${oauth.kakao.redirect-uri}")
    private String redirectUri;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public OAuthUserInfo getUserInfo(String authorizationCode) {
        String accessToken = getAccessToken(authorizationCode);
        return fetchUserInfo(accessToken);
    }

    /* 1. Authorization Code → Access Token */
    private String getAccessToken(String code) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", clientId);
        body.add("redirect_uri", redirectUri);
        body.add("code", code);

        // Client Secret을 쓰는 경우만 포함
        if (clientSecret != null && !clientSecret.isBlank()) {
            body.add("client_secret", clientSecret);
        }

        HttpEntity<MultiValueMap<String, String>> request =
                new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                tokenUri,
                HttpMethod.POST,
                request,
                Map.class
        );

        if (response.getBody() == null || response.getBody().get("access_token") == null) {
            throw new IllegalArgumentException("Failed to retrieve Kakao access token");
        }

        return response.getBody().get("access_token").toString();
    }

    /* 2. Access Token → Kakao User Info */
    @SuppressWarnings("unchecked")
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
            throw new IllegalArgumentException("Failed to retrieve Kakao user info");
        }

        Map<String, Object> body = response.getBody();
        Map<String, Object> kakaoAccount =
                (Map<String, Object>) body.get("kakao_account");

        String email = null;
        String nickname = null;

        if (kakaoAccount != null) {
            email = (String) kakaoAccount.get("email");

            Map<String, Object> profile =
                    (Map<String, Object>) kakaoAccount.get("profile");
            if (profile != null) {
                nickname = (String) profile.get("nickname");
            }
        }

        if (nickname == null || nickname.isBlank()) {
            nickname = "kakao_user";
        }

        return OAuthUserInfo.builder()
                .provider("kakao")
                .providerId(body.get("id").toString())
                .email(email)          // nullable (정상)
                .name(nickname)        // nickname 사용
                .build();
    }
}
