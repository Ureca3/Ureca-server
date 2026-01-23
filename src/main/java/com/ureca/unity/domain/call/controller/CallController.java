package com.ureca.unity.domain.call.controller;

import com.ureca.unity.domain.call.dto.response.AgoraTokenResponse;
import com.ureca.unity.domain.call.service.CallService;
import com.ureca.unity.global.exception.CustomException;
import com.ureca.unity.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/agora")
@RequiredArgsConstructor
public class CallController {

    private final CallService callService;

    @GetMapping("/token")
    public ResponseEntity<AgoraTokenResponse> getRtcToken(
            @RequestParam String channel,
            @RequestParam(defaultValue = "0") int uid) {
        System.out.println("토큰 요청: "+uid+", 채널: "+channel);
        AgoraTokenResponse value=callService.makeToken(channel,uid);
        return ResponseEntity.ok(value);
    }
}
