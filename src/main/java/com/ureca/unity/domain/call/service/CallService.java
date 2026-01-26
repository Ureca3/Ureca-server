package com.ureca.unity.domain.call.service;

import com.ureca.unity.domain.call.dto.response.AgoraTokenResponse;

public interface CallService {
    AgoraTokenResponse makeToken(String channel, int uid);
}
