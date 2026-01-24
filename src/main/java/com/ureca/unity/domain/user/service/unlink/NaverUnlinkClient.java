package com.ureca.unity.domain.user.service.unlink;

import com.ureca.unity.domain.auth.model.OAuthToken;
import com.ureca.unity.domain.user.model.User;
import com.ureca.unity.global.exception.CustomException;
import com.ureca.unity.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component("naver")
@RequiredArgsConstructor
public class NaverUnlinkClient implements SocialUnlinkClient {

    @Value("${oauth.naver.client-id}")
    private String clientId;

    @Value("${oauth.naver.client-secret}")
    private String clientSecret;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public void unlink(User user, OAuthToken token) {
        if (token.getAccessToken() == null || token.getAccessToken().isBlank()) {
            throw new CustomException(ErrorCode.OAUTH_TOKEN_NOT_FOUND);
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "delete");
            body.add("client_id", clientId);
            body.add("client_secret", clientSecret);
            body.add("access_token", token.getAccessToken());

            HttpEntity<MultiValueMap<String, String>> req = new HttpEntity<>(body, headers);

            ResponseEntity<String> res = restTemplate.exchange(
                    "https://nid.naver.com/oauth2.0/token",
                    HttpMethod.POST,
                    req,
                    String.class
            );

            if (!res.getStatusCode().is2xxSuccessful()) {
                throw new CustomException(ErrorCode.SOCIAL_UNLINK_FAILED);
            }
        } catch (Exception e) {
            throw new CustomException(ErrorCode.SOCIAL_UNLINK_FAILED);
        }
    }
}
