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

@Component("kakaoUnlink")
@RequiredArgsConstructor
public class KakaoUnlinkClient implements SocialUnlinkClient {

    @Value("${oauth.kakao.admin-key}")
    private String adminKey;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public void unlink(User user, OAuthToken token) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("Authorization", "KakaoAK " + adminKey);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("target_id_type", "user_id");
            body.add("target_id", user.getProviderId());

            HttpEntity<MultiValueMap<String, String>> req = new HttpEntity<>(body, headers);

            ResponseEntity<String> res = restTemplate.exchange(
                    "https://kapi.kakao.com/v1/user/unlink",
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
