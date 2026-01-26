package com.ureca.unity.domain.user.service.unlink;

import com.ureca.unity.domain.auth.model.OAuthToken;
import com.ureca.unity.domain.user.model.User;

public interface SocialUnlinkService {
    void unlink(User user, OAuthToken token);
}
