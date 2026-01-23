package com.ureca.unity.domain.stt.service;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.api.gax.longrunning.OperationFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.speech.v1.*;
import com.google.protobuf.ByteString;
import com.ureca.unity.domain.stt.mapper.CounselingResultMapper;
import com.ureca.unity.domain.stt.model.CounselingResult;
import com.ureca.unity.domain.summary.service.SummaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SttServiceImpl implements SttService {

    private final CounselingResultMapper sttMapper;
    private final SummaryService summaryService;

    @Value("${google.cloud.credentials.location}")
    private String keyPath;

    @Override
    public CounselingResult startStt(File file) {
        // 초기 작업 저장
        CounselingResult job = CounselingResult.builder()
                .userId(1L)
                .counselorId(1L)
                .counselingType("CALL")
                .status("LOADING")
                .build();
        sttMapper.insert(job);

        try {
            // 1. 오디오 파일 유효성 검사 및 읽기
            byte[] data = Files.readAllBytes(file.toPath());
            if (data.length == 0) throw new RuntimeException("파일이 비어있습니다.");

            log.info("STT 시작 - 파일 크기: {} bytes", data.length);
            ByteString audioBytes = ByteString.copyFrom(data);

            RecognitionAudio audio = RecognitionAudio.newBuilder()
                    .setContent(audioBytes)
                    .build();

            // 2. 설정 최적화 (가장 범용적인 설정)
            RecognitionConfig config = RecognitionConfig.newBuilder()
                    // 브라우저 녹음 파일(WebM/Wav) 헤더를 자동 감지하도록 설정
                    .setEncoding(RecognitionConfig.AudioEncoding.ENCODING_UNSPECIFIED)
                    .setLanguageCode("ko-KR")
                    .setSampleRateHertz(16000) // 특정 포맷이 아니면 생략하는 것이 안전
                    .setAudioChannelCount(1) // 아고라 녹음은 보통 단일 채널
                    .build();

            // 3. Google 인증 로드
            Resource resource = new DefaultResourceLoader().getResource(keyPath);
            try (InputStream is = resource.getInputStream()) {
                GoogleCredentials credentials = GoogleCredentials.fromStream(is);
                SpeechSettings settings = SpeechSettings.newBuilder()
                        .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                        .build();

                try (SpeechClient speechClient = SpeechClient.create(settings)) {
                    log.info("Google STT 비동기 요청 전송 중...");

                    // 4. 비동기 요청 실행 (긴 파일 대응)
                    OperationFuture<LongRunningRecognizeResponse, LongRunningRecognizeMetadata> responseFuture =
                            speechClient.longRunningRecognizeAsync(config, audio);

                    // 5. 완료될 때까지 기다림 (여기서 블로킹되어야 로그가 남고 파일 삭제가 안전함)
                    LongRunningRecognizeResponse response = responseFuture.get();

                    // 결과 추출 및 로그
                    String text = response.getResultsList().stream()
                            .map(r -> r.getAlternatives(0).getTranscript())
                            .collect(Collectors.joining(" "));

                    if (text.trim().isEmpty()) {
                        log.warn("STT 결과가 비어있습니다. 오디오 데이터 확인 필요.");
                        job.setStatus("FAIL");
                        job.setTexts("No speech detected.");
                    } else {
                        log.info("STT 변환 완료: {}", text);
                        job.setStatus("SUCCESS");
                        job.setTexts(text);
                    }
                }
            }

        } catch (Exception e) {
            log.error("STT 처리 중 치명적 오류: ", e);
            job.setStatus("FAIL");
            job.setTexts("Error: " + e.getMessage());
        } finally {
            // 모든 작업이 끝난 후 파일 삭제
//            if (file.exists()) {
//                boolean isDeleted = file.delete();
//                log.info("임시 파일 삭제 여부: {}", isDeleted);
//            }
        }

        // 6. DB 업데이트 및 후속 작업
        sttMapper.updateResult(job);

        // 성공 시에만 요약 서비스 호출
        if ("SUCCESS".equals(job.getStatus())) {
            summaryService.createSummary(job.getCounselingResultId(),job.getUserId(),job.getTexts());
        }

        return job;
    }

    @Override
    public CounselingResult getStt(Long counselingResultId) {
        return sttMapper.findByCounselingId(counselingResultId);
    }
}