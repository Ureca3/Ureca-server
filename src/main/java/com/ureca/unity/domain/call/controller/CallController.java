package com.ureca.unity.domain.call.controller;

import com.ureca.unity.domain.call.util.RtcTokenBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/agora")
public class CallController {

    @Value("${agora.appId}")
    private String appId;

    @Value("${agora.appCert}")
    private String appCert;

    @GetMapping("/token")
    public Map<String, String> getRtcToken(
            @RequestParam String channel,
            @RequestParam(defaultValue = "0") int uid) {
        System.out.println("토큰 요청: "+uid+", 채널: "+channel);
        RtcTokenBuilder tokenBuilder = new RtcTokenBuilder();
        RtcTokenBuilder.Role role = RtcTokenBuilder.Role.Role_Publisher;
        int expire = 3600*2;
        int timestamp = (int) (System.currentTimeMillis() / 1000 + expire);

        String token = tokenBuilder.buildTokenWithUid(appId, appCert, channel, uid, role, expire);
        return Map.of("appId", appId, "token", token);
    }
}
