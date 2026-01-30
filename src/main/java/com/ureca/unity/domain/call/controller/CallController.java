package com.ureca.unity.domain.call.controller;

import com.ureca.unity.domain.call.dto.response.AgoraTokenResponse;
import com.ureca.unity.domain.call.service.CallService;
import com.ureca.unity.global.exception.CustomException;
import com.ureca.unity.global.exception.ErrorCode;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(
        name = "3. Call",
        description = "전화 연결"
)
@RestController
@RequestMapping("/api/agora")
@RequiredArgsConstructor
@Slf4j
public class CallController {

    private final CallService callService;

    @GetMapping("/token")
    public ResponseEntity<AgoraTokenResponse> getRtcToken(
            @RequestParam String channel,
            @RequestParam(defaultValue = "0") int uid) {
        log.debug("토큰 요청: {}, 채널: {}",uid,channel);
        AgoraTokenResponse value=callService.makeToken(channel,uid);
        return ResponseEntity.ok(value);
    }
}
