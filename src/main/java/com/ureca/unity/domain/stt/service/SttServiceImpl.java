package com.ureca.unity.domain.stt.service;

import com.google.cloud.speech.v1.*;
import com.google.protobuf.ByteString;
import com.ureca.unity.domain.stt.mapper.SttMapper;
import com.ureca.unity.domain.stt.model.SttJob;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SttServiceImpl implements SttService {

    private final SttMapper sttMapper;

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

        try {

            String audioPath = "C:/Users/bagju/Desktop/realsample.wav";

            byte[] data = Files.readAllBytes(Paths.get(audioPath));
            ByteString audioBytes = ByteString.copyFrom(data);

            RecognitionAudio audio = RecognitionAudio.newBuilder()
                    .setContent(audioBytes)
                    .build();


            RecognitionConfig config = RecognitionConfig.newBuilder()
                    .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                    .setLanguageCode("ko-KR")
                    .setAudioChannelCount(2)                      // stereo
                    .setEnableSeparateRecognitionPerChannel(true) // 채널 분리
                    // sampleRateHertz는 WAV 헤더 기준 → 명시 안 함
                    .build();

            try (SpeechClient speechClient = SpeechClient.create()) {

                RecognizeResponse response =
                        speechClient.recognize(config, audio);

                // 채널 상관없이 전부 합침 (지금은 검증 목적)
                String text = response.getResultsList().stream()
                        .map(r -> r.getAlternatives(0).getTranscript())
                        .collect(Collectors.joining(" "));

                job.setStatus("SUCCESS");
                job.setSummaryText(text);
            }

        } catch (Exception e) {
            e.printStackTrace();
            job.setStatus("FAIL");
            job.setSummaryText(null);
        }

        sttMapper.updateResult(job);
        return job;
    }

    @Override
    public SttJob getStt(Long counselingId) {
        return sttMapper.findByCounselingId(counselingId);
    }
}
