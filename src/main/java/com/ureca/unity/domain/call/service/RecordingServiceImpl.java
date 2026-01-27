package com.ureca.unity.domain.call.service;

import com.ureca.unity.domain.call.util.Converter;
import com.ureca.unity.domain.stt.service.SttService;
import com.ureca.unity.global.exception.CustomException;
import com.ureca.unity.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.File;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class RecordingServiceImpl implements RecordingService {

    @Value("${agora.appId}") private String appId;
    @Value("${agora.customerId}") private String customerId;
    @Value("${agora.customerSecret}") private String customerSecret;

    @Value("${aws.s3.access-key}") private String s3Key;
    @Value("${aws.s3.secret-key}") private String s3Secret;
    @Value("${aws.s3.bucket}") private String s3Bucket;
    @Value("${aws.s3.region}") private int s3Region;

    private final WebClient.Builder webClientBuilder;
    private final SttService sttService;

    //@Value 주입이 완료된 후, 호출 시점에 WebClient 빌드
    private WebClient getWebClient() {
        String authStr = customerId + ":" + customerSecret;
        String auth = Base64.getEncoder().encodeToString(authStr.getBytes());

        return webClientBuilder
                .baseUrl("https://api.agora.io/v1/apps/" + appId)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + auth)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public String acquire(String channelName, String uid) {
        log.info("[Agora] Acquire 요청 시작 - channel: {}, uid: {}", channelName, uid);
        // 전체 Body
        Map<String, Object> body = Map.of(
                "cname",channelName,
                "uid", uid,
                "clientRequest",Map.of(
                        "resourceExpiredHour",24,
                        "scene",0
                )
        );
        
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
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public String start(String resourceId, String channelName, String uid, String token) {
        log.info("[Agora] Start 요청 시작 - resourceId: {}, channel: {}", resourceId, channelName);

        // 녹음 및 저장 config들
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
                "audioProfile", 2,
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
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void stop(String resourceId, String sid, String channelName, String uid, String userId) {
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

            //s3의 파일->wav로 변경->stt
            CompletableFuture.runAsync(() -> {
                try {
                    Thread.sleep(10000); //TODO:차후 lambda로 호출되면 동작하도록 개선
                    String m3u8Url = String.format("https://%s.s3.ap-northeast-2.amazonaws.com/recordings/%s/%s_%s.m3u8",
                            s3Bucket, channelName, sid, channelName);
                    log.info("변환 시작: {}", m3u8Url);
                    File wavFile = new Converter().convertM3u8ToWav(m3u8Url);

                    if (wavFile != null && wavFile.exists()) {
                        log.info("최종 WAV 생성 성공: {}", wavFile.getAbsolutePath());
                        sttService.startStt(wavFile, Long.parseLong(userId));
                    }
                } catch (Exception e) {
                    log.error("비동기 변환 작업 중 오류: {}", e);
                }
            });

        } catch (Exception e) {
            log.error("[Agora] Stop 실패: {}", e.getMessage());
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}