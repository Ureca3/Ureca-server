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

@Component("kakao")
@RequiredArgsConstructor
public class KakaoOAuthClient implements OAuthClient {

    @Value("${oauth.kakao.client-id}")
    private String clientId;

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
        body.add("redirect_uri", redirectUri);
        body.add("code", code);

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

        if (response.getBody() == null || response.getBody().get("id") == null) {
            throw new IllegalArgumentException("Failed to retrieve Kakao user info");
        }

        Map<String, Object> body = response.getBody();
        Map<String, Object> kakaoAccount =
                (Map<String, Object>) body.get("kakao_account");

        String email = null;
        String nickname = null;

        if (kakaoAccount != null) {

            Boolean hasEmail = (Boolean) kakaoAccount.get("has_email");
            if (Boolean.TRUE.equals(hasEmail)) {
                email = (String) kakaoAccount.get("email");
            }

            Map<String, Object> profile =
                    (Map<String, Object>) kakaoAccount.get("profile");
            if (profile != null) {
                nickname = (String) profile.get("nickname");
            }
        }

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Kakao email consent required");
        }

        if (nickname == null || nickname.isBlank()) {
            nickname = "kakao_user";
        }
        return OAuthUserInfo.builder()
                .provider("kakao")
                .providerId(body.get("id").toString())
                .email(email)
                .name(nickname)
                .build();
    }
}
