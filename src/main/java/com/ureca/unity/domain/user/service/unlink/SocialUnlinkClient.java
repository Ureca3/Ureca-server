package com.ureca.unity.domain.user.service.unlink;

import com.ureca.unity.domain.auth.model.OAuthToken;
import com.ureca.unity.domain.user.model.User;

public interface SocialUnlinkClient {
    void unlink(User user, OAuthToken token); // token nullable 아님(구글/네이버), 카카오는 token 없어도 되지만 여기선 받게 둠
}
