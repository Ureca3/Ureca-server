package com.ureca.unity.domain.user.service.unlink;

import com.ureca.unity.domain.auth.model.OAuthToken;
import com.ureca.unity.domain.user.model.User;
import com.ureca.unity.global.exception.CustomException;
import com.ureca.unity.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class SocialUnlinkServiceImpl implements SocialUnlinkService {

    private final Map<String, SocialUnlinkClient> unlinkClients;

    @Override
    public void unlink(User user, OAuthToken token) {
        String provider = user.getProvider() == null ? "" : user.getProvider().toLowerCase();
        String key = provider + "Unlink";

        SocialUnlinkClient client = unlinkClients.get(key);
        if (client == null) throw new CustomException(ErrorCode.INVALID_OAUTH_PROVIDER);

        client.unlink(user, token);
    }
}
