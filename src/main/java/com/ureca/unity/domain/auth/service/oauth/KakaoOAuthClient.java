package com.ureca.unity.domain.auth.service.oauth;

import com.ureca.unity.domain.auth.constant.OAuthProvider;
import com.ureca.unity.domain.auth.dto.KakaoUserResponse;
import com.ureca.unity.domain.auth.dto.OAuthUserInfo;
import com.ureca.unity.domain.auth.dto.TokenResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component("kakao")
@RequiredArgsConstructor
public class KakaoOAuthClient implements OAuthClient {

    @Value("${oauth.kakao.client-id}")
    private String clientId;

    @Value("${oauth.kakao.client-secret}")
    private String clientSecret;

    @Value("${oauth.kakao.redirect-uri}")
    private String redirectUri;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public OAuthUserInfo getUserInfo(String code) {
        String accessToken = getAccessToken(code);
        if (accessToken == null || accessToken.isEmpty()) {
            throw new RuntimeException("Failed to get Kakao access token");
        }
        return getUser(accessToken);
    }

    private String getAccessToken(String code) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("redirect_uri", redirectUri);
        params.add("code", code);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        // TokenResponse는 Jackson이 인식할 수 있도록 기본 생성자 필요
        TokenResponse response = restTemplate.postForObject(
                "https://kauth.kakao.com/oauth/token",
                request,
                TokenResponse.class
        );

        if (response == null || response.getAccessToken() == null) {
            throw new RuntimeException("Kakao token response is null or invalid");
        }

        return response.getAccessToken();
    }

    private OAuthUserInfo getUser(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<KakaoUserResponse> response = restTemplate.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.GET,
                request,
                KakaoUserResponse.class
        );

        KakaoUserResponse body = response.getBody();
        if (body == null) throw new RuntimeException("Kakao user response is null");

        return new OAuthUserInfo(
                OAuthProvider.KAKAO.value(),
                String.valueOf(body.getId()),
                body.getKakaoAccount().getEmail(),
                body.getKakaoAccount().getProfile().getNickname()
        );
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class TokenResponse {
        private String access_token;
        private String token_type;
        private String refresh_token;
        private Long expires_in;
        private String scope;

        public String getAccessToken() {
            return access_token;
        }
    }
}
