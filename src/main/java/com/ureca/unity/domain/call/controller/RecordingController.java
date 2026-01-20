package com.ureca.unity.domain.call.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/agora/record")
public class RecordingController {

    @Value("${agora.appId}") private String appId;
    @Value("${agora.appCert}") private String appCert;

    @PostMapping("/start")
    public Map<String, Object> startRecording(@RequestBody Map<String, String> body) {
        System.out.println("녹화 시작");
        String channel = body.get("channel");
        String customerUid = "1000"; // 가상 유저

        // 1️⃣ Acquire
        // 2️⃣ Start Recording
        // TODO: HTTP 요청으로 Agora Cloud Recording REST API 호출
        // 반환값: resourceId, sid
        return Map.of("resourceId", "resource123", "sid", "sid123");
    }

    @PostMapping("/stop")
    public Map<String, Object> stopRecording(@RequestBody Map<String, String> body) {
        System.out.println("녹화 종료");
        String channel = body.get("channel");
        String resourceId = body.get("resourceId");
        String sid = body.get("sid");

        // Agora Cloud Recording stop API 호출
        // 반환값: 녹음 파일 URL
        return Map.of("recordingUrl", "https://s3.amazonaws.com/your-bucket/record.mp3");
    }
}
