package com.ureca.unity.domain.user.service.unlink;

import com.ureca.unity.domain.auth.model.OAuthToken;
import com.ureca.unity.domain.user.model.User;
import com.ureca.unity.global.exception.CustomException;
import com.ureca.unity.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component("googleUnlink")
@RequiredArgsConstructor
public class GoogleUnlinkClient implements SocialUnlinkClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public void unlink(User user, OAuthToken token) {
        String toRevoke = (token.getRefreshToken() != null && !token.getRefreshToken().isBlank())
                ? token.getRefreshToken()
                : token.getAccessToken();

        if (toRevoke == null || toRevoke.isBlank()) {
            throw new CustomException(ErrorCode.OAUTH_TOKEN_NOT_FOUND);
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("token", toRevoke);

            HttpEntity<MultiValueMap<String, String>> req = new HttpEntity<>(body, headers);

            ResponseEntity<String> res = restTemplate.exchange(
                    "https://oauth2.googleapis.com/revoke",
                    HttpMethod.POST,
                    req,
                    String.class
            );

            if (!res.getStatusCode().is2xxSuccessful()) {
                throw new CustomException(ErrorCode.SOCIAL_UNLINK_FAILED);
            }
        } catch (Exception e) {
            if (e instanceof CustomException) {
                throw (CustomException) e;
            }
            throw new CustomException(ErrorCode.SOCIAL_UNLINK_FAILED);
        }
    }
}
