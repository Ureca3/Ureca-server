package com.ureca.unity.domain.stt.service;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.speech.v1.*;
import com.google.protobuf.ByteString;
import com.ureca.unity.domain.stt.mapper.SttMapper;
import com.ureca.unity.domain.stt.model.SttJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import ws.schild.jave.Encoder;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SttServiceImpl implements SttService {

    private final SttMapper sttMapper;

    @Value("${google.cloud.credentials.location}")
    private String keyPath;

    @Override
    public SttJob startStt(Long counselingId) {
        Long userId = 1L; // 임시

        SttJob job = sttMapper.findByCounselingId(counselingId);

        if (job == null) {
            job = SttJob.builder()
                    .counselingId(counselingId)
                    .userId(userId)
                    .summaryType("CALL")
                    .status("LOADING")
                    .build();
            sttMapper.insert(job);
        }

        // 1. 오디오 파일 읽기
//      String audioPath = "C:/Users/food0/Desktop/realsample.wav";
        Path m4aPath = Paths.get("C:/Users/food0/Desktop/테헤란로.m4a");
        File tempWavFile = null;

        try {
            tempWavFile = convertM4aToWav(m4aPath.toFile());

            byte[] data = Files.readAllBytes(tempWavFile.toPath());
            ByteString audioBytes = ByteString.copyFrom(data);

            RecognitionAudio audio = RecognitionAudio.newBuilder()
                    .setContent(audioBytes)
                    .build();

            RecognitionConfig config = RecognitionConfig.newBuilder()
                    .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                    .setLanguageCode("ko-KR")
                    .setSampleRateHertz(16000)
                    .setAudioChannelCount(1)
                    .build();

            // 2. Google STT 인증 및 실행
            Resource resource = new DefaultResourceLoader().getResource(keyPath);
            try (InputStream is = resource.getInputStream()) {
                GoogleCredentials credentials = GoogleCredentials.fromStream(is);
                SpeechSettings settings = SpeechSettings.newBuilder()
                        .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                        .build();

                try (SpeechClient speechClient = SpeechClient.create(settings)) {
                    RecognizeResponse response = speechClient.recognize(config, audio);

                    String text = response.getResultsList().stream()
                            .map(r -> r.getAlternatives(0).getTranscript())
                            .collect(Collectors.joining(" "));

                    job.setStatus("SUCCESS");
                    job.setSummaryText(text);
                }
            } // InputStream close

        } catch (Exception e) {
            log.error("STT 처리 중 오류 발생: ", e);
            job.setStatus("FAIL"); // 에러 시 상태 업데이트
            job.setSummaryText("Error: " + e.getMessage());
        } finally {
            // 4. 사용 후 임시 wav 파일 삭제 (서버 용량 관리)
            if (tempWavFile != null && tempWavFile.exists()) {
                tempWavFile.delete();
                log.info("임시 변환 파일 삭제 완료");
            }
        }

        // 3. 결과 DB 업데이트
        sttMapper.updateResult(job);
        return job;
    }

    private File convertM4aToWav(File source) throws Exception {
        File target = File.createTempFile("stt_temp_", ".wav");

        AudioAttributes audio = new AudioAttributes();
        audio.setCodec("pcm_s16le"); // Google STT 권장 코덱
        audio.setSamplingRate(16000);
        audio.setChannels(1);

        EncodingAttributes attrs = new EncodingAttributes();
        attrs.setOutputFormat("wav");
        attrs.setAudioAttributes(audio);

        Encoder encoder = new Encoder();
        encoder.encode(new MultimediaObject(source), target, attrs);

        log.info("변환 완료: {} -> {}", source.getName(), target.getName());
        return target;
    }

    @Override
    public SttJob getStt(Long counselingId) {
        return sttMapper.findByCounselingId(counselingId);
    }
}