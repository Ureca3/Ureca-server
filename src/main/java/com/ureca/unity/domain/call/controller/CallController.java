package com.ureca.unity.domain.call.controller;

import com.ureca.unity.domain.call.util.RtcTokenBuilder2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ws.schild.jave.Encoder;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;

import java.io.File;
import java.net.URL;
import java.util.Map;

@RestController
@RequestMapping("/api/agora")
@Slf4j
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
        RtcTokenBuilder2 tokenBuilder = new RtcTokenBuilder2();
        RtcTokenBuilder2.Role role = RtcTokenBuilder2.Role.ROLE_PUBLISHER;
        int expirationTimeInSeconds = 3600;
        int timestamp = (int) (System.currentTimeMillis() / 1000 + expirationTimeInSeconds);

        String token = tokenBuilder.buildTokenWithUid(appId, appCert, channel, uid, role, timestamp,timestamp);
        return Map.of("appId", appId, "token", token);
    }

    public File convertM3u8ToWav(String m3u8Url) {
        try {
            // 1. 결과물이 저장될 임시 파일 생성
            File target = File.createTempFile("recording_", ".wav");

            // 2. 오디오 설정 (Google/Whisper STT 권장 사양)
            AudioAttributes audio = new AudioAttributes();
            audio.setCodec("pcm_s16le"); // WAV 표준 코덱
            audio.setSamplingRate(16000); // 16kHz (STT 최적화)
            audio.setChannels(1);         // 모노 (화자 분리 및 인식률 향상)

            // 3. 인코딩 설정
            EncodingAttributes attrs = new EncodingAttributes();
            attrs.setOutputFormat("wav");
            attrs.setAudioAttributes(audio);

            // 4. 변환 실행 (URL로부터 직접 읽기)
            Encoder encoder = new Encoder();
            // 아고라 S3 URL을 MultimediaObject에 직접 넣습니다.
            encoder.encode(new MultimediaObject(new URL(m3u8Url)), target, attrs);

            log.info("WAV 변환 성공: {}", target.getAbsolutePath());
            return target;

        } catch (Exception e) {
            log.error("변환 실패: {}", e.getMessage());
            return null;
        }
    }
}
