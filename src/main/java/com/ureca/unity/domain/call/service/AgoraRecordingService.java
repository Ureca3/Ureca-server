package com.ureca.unity.domain.call.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import ws.schild.jave.Encoder;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;

import java.io.File;
import java.net.URL;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class AgoraRecordingService {

    @Value("${agora.appId}") private String appId;
    @Value("${agora.customerId}") private String customerId;
    @Value("${agora.customerSecret}") private String customerSecret;

    @Value("${aws.s3.access-key}") private String s3Key;
    @Value("${aws.s3.secret-key}") private String s3Secret;
    @Value("${aws.s3.bucket}") private String s3Bucket;
    @Value("${aws.s3.region}") private int s3Region;

    private final WebClient.Builder webClientBuilder;

    // 생성자에서는 Builder만 주입받습니다.
    public AgoraRecordingService(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    /**
     * @Value 주입이 완료된 후, 호출 시점에 WebClient를 빌드합니다.
     */
    private WebClient getWebClient() {
        String authStr = customerId + ":" + customerSecret;
        String auth = Base64.getEncoder().encodeToString(authStr.getBytes());

        return webClientBuilder
                .baseUrl("https://api.agora.io/v1/apps/" + appId)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + auth)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    /**
     * 1. Acquire Resource ID
     */
    public String acquire(String channelName, String uid) {
        log.info("[Agora] Acquire 요청 시작 - channel: {}, uid: {}", channelName, uid);

        // 1. 내부 clientRequest 객체 생성
        Map<String, Object> clientRequest = new HashMap<>();
        clientRequest.put("resourceExpiredHour", 24);
        clientRequest.put("scene", 0); // 요청하신 규격에 맞춤
        // 2. 전체 Body 구성
        Map<String, Object> body = new HashMap<>();
        body.put("cname", channelName);
        body.put("uid", uid);
        body.put("clientRequest", clientRequest);

        try {
            Map<String, Object> response = getWebClient().post()
                    .uri("/cloud_recording/acquire")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block(Duration.ofSeconds(10));

            String resourceId = (String) Objects.requireNonNull(response).get("resourceId");
            log.info("[Agora] Acquire 성공 - resourceId: {}", resourceId);
            return resourceId;
        } catch (Exception e) {
            log.error("[Agora] Acquire 실패: {}", e.getMessage());
            throw new RuntimeException("Agora Acquire API 호출 실패", e);
        }
    }

    /**
     * 2. Start Recording
     */
    public String start(String resourceId, String channelName, String uid, String token) {
        log.info("[Agora] Start 요청 시작 - resourceId: {}, channel: {}", resourceId, channelName);

        Map<String, Object> storageConfig = Map.of(
                "vendor", 1,
                "region", s3Region,
                "bucket", s3Bucket,
                "accessKey", s3Key,
                "secretKey", s3Secret,
                "fileNamePrefix", List.of("recordings", channelName),
                "extensionParams",Map.of("acl","public-read")
        );

        Map<String, Object> recordingConfig = Map.of(
                "maxIdleTime", 30,
                "streamTypes", 0,
                "audioProfile", 1,
                "channelType", 0
        );

        Map<String, Object> body = Map.of(
                "cname", channelName,
                "uid", uid,
                "clientRequest", Map.of(
                        "token", token,
                        "recordingConfig", recordingConfig,
                        "storageConfig", storageConfig
                )
        );

        try {
            Map<String, Object> response = getWebClient().post()
                    .uri("/cloud_recording/resourceid/{resourceId}/mode/mix/start", resourceId)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block(Duration.ofSeconds(10));

            String sid = (String) Objects.requireNonNull(response).get("sid");
            log.info("[Agora] Start 성공 - sid: {}", sid);
            return sid;
        } catch (Exception e) {
            log.error("[Agora] Start 실패: {}", e.getMessage());
            throw new RuntimeException("Agora Start API 호출 실패", e);
        }
    }

    /**
     * 3. Stop Recording
     */
    public void stop(String resourceId, String sid, String channelName, String uid) {
        log.info("[Agora] Stop 요청 - sid: {}", sid);

        Map<String, Object> body = Map.of(
                "cname", channelName,
                "uid", uid,
                "clientRequest", Map.of()
        );

        try {
            getWebClient().post()
                    .uri("/cloud_recording/resourceid/{resourceId}/sid/{sid}/mode/mix/stop", resourceId, sid)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block(Duration.ofSeconds(10));
            log.info("[Agora] Stop 성공");

            CompletableFuture.runAsync(() -> {
                try {
                    Thread.sleep(10000);

                    // 1. 실제 S3 URL 생성 (이 주소는 이제 우리 서버 IP에서만 유효함)
                    String m3u8Url = String.format("https://%s.s3.ap-northeast-2.amazonaws.com/recordings/%s/%s_%s.m3u8",
                            s3Bucket, channelName, sid, channelName);

                    log.info("변환 시작: {}", m3u8Url);

                    File wavFile = convertM3u8ToWav(m3u8Url);

                    if (wavFile != null && wavFile.exists()) {
                        log.info("최종 WAV 생성 성공: {}", wavFile.getAbsolutePath());
                        // 4. STT 호출 및 파일 정리...
                    }
                } catch (Exception e) {
                    log.error("비동기 변환 작업 중 오류: {}", e.getMessage());
                }
            });

        } catch (Exception e) {
            log.error("[Agora] Stop 실패: {}", e.getMessage());
            throw new RuntimeException("Agora Stop API 호출 실패", e);
        }
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