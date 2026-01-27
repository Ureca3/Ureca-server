package com.ureca.unity.domain.call.service;

import com.ureca.unity.domain.call.dto.response.AgoraTokenResponse;
import com.ureca.unity.domain.call.util.token.RtcTokenBuilder2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CallServiceImpl implements CallService{

    @Value("${agora.appId}")
    private String appId;

    @Value("${agora.appCert}")
    private String appCert;

    @Override
    public AgoraTokenResponse makeToken(String channel, int uid) {
        RtcTokenBuilder2 tokenBuilder = new RtcTokenBuilder2();
        RtcTokenBuilder2.Role role = RtcTokenBuilder2.Role.ROLE_PUBLISHER;
        int expirationTimeInSeconds = 3600;

        String token = tokenBuilder.buildTokenWithUid(appId, appCert, channel, uid, role, expirationTimeInSeconds,expirationTimeInSeconds);
        return new AgoraTokenResponse(appId,token);
    }
}
