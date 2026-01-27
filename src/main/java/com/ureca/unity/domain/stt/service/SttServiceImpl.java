package com.ureca.unity.domain.stt.service;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.api.gax.longrunning.OperationFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.speech.v1.*;
import com.ureca.unity.domain.stt.mapper.CounselingResultMapper;
import com.ureca.unity.domain.stt.model.CounselingResult;
import com.ureca.unity.domain.summary.service.SummaryService;
import com.ureca.unity.domain.call.util.GcsUploader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SttServiceImpl implements SttService {

    private final CounselingResultMapper sttMapper;
    private final SummaryService summaryService;
    private final GcsUploader gcpUploader;

    @Value("${google.cloud.credentials.location}")
    private String keyPath;

    @Override
    public CounselingResult startStt(File file, long userId, CounselingResult job) {
        String gcsUri = null;

        try {
            if (!file.exists() || file.length() == 0) {
                throw new RuntimeException("오디오 파일이 존재하지 않거나 비어있습니다.");
            }

            log.info("STT 시작 (Long Audio) - file: {}, size: {} bytes",
                    file.getAbsolutePath(), file.length());

            // 1️⃣ GCS 업로드
            String objectName = "recordings/"
                    + userId + "/"
                    + job.getCounselingResultId() + ".wav";

            gcsUri = gcpUploader.uploadWav(
                    objectName,
                    file.toPath()
            );

            log.info("GCS 업로드 완료: {}", gcsUri);

            // 2️⃣ RecognitionAudio (URI 기반)
            RecognitionAudio audio = RecognitionAudio.newBuilder()
                    .setUri(gcsUri)
                    .build();

            // 3️⃣ Long Audio 최적화 설정
            RecognitionConfig config = RecognitionConfig.newBuilder()
                    .setLanguageCode("ko-KR")
                    .setEncoding(RecognitionConfig.AudioEncoding.ENCODING_UNSPECIFIED)
//                    .setSampleRateHertz(16000) // wav 실제 값과 반드시 일치
                    .setAudioChannelCount(1)
                    .setEnableAutomaticPunctuation(true)
                    .setUseEnhanced(true)
                    .setModel("latest_long")
                    .build();

            // 4️⃣ Google 인증
            Resource resource = new DefaultResourceLoader().getResource(keyPath);
            try (InputStream is = resource.getInputStream()) {

                GoogleCredentials credentials = GoogleCredentials.fromStream(is);
                SpeechSettings settings = SpeechSettings.newBuilder()
                        .setCredentialsProvider(
                                FixedCredentialsProvider.create(credentials))
                        .build();

                try (SpeechClient speechClient = SpeechClient.create(settings)) {

                    log.info("Google STT longRunningRecognize 요청 전송");

                    OperationFuture<
                            LongRunningRecognizeResponse,
                            LongRunningRecognizeMetadata
                            > future =
                            speechClient.longRunningRecognizeAsync(config, audio);

                    // ⏳ 긴 파일 대기 (최대 30분)
                    LongRunningRecognizeResponse response =
                            future.get(30, TimeUnit.MINUTES);

                    String text = response.getResultsList().stream()
                            .map(r -> r.getAlternatives(0).getTranscript())
                            .collect(Collectors.joining(" "));

                    if (text.isBlank()) {
                        log.warn("STT 결과가 비어있음");
                        job.setStatus("FAIL");
                        job.setTexts("No speech detected.");
                    } else {
                        log.info("STT 완료 (length={} chars)", text.length());
                        job.setStatus("SUCCESS");
                        job.setTexts(text);
                    }
                }
            }

        } catch (Exception e) {
            log.error("STT 처리 중 오류 발생", e);
            job.setStatus("FAIL");
            job.setTexts("Error: " + e.getMessage());

        } finally {
            // 5️⃣ 로컬 파일 삭제
            if (file.exists()) {
                boolean deleted = file.delete();
                log.info("로컬 wav 파일 삭제: {}", deleted);
            }
        }

        // 6️⃣ DB 업데이트
        sttMapper.updateResult(job);

        // 7️⃣ 성공 시 요약 호출
        if ("SUCCESS".equals(job.getStatus())) {
            summaryService.createSummary(
                    job.getCounselingResultId(),
                    job.getUserId(),
                    job.getTexts()
            );
        }

        return job;
    }


    @Override
    public CounselingResult getStt(Long counselingResultId) {
        return sttMapper.findByCounselingId(counselingResultId);
    }
}