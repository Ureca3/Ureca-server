package com.ureca.unity.domain.call.util;

import lombok.extern.slf4j.Slf4j;
import ws.schild.jave.Encoder;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;

import java.io.File;
import java.net.URL;

@Slf4j
public class Converter {
    public File convertM3u8ToWav(String m3u8Url) {
        File target=null;
        try {
            // 1. 결과물이 저장될 임시 파일 생성
            target = File.createTempFile("recording_", ".wav");

            // 2. 오디오 설정 (Google/Whisper STT 권장 사양)
            AudioAttributes audio = new AudioAttributes();
//            audio.setCodec("pcm_s16le"); // WAV 표준 코덱
            audio.setSamplingRate(16000); // 16kHz (STT 최적화)
            audio.setBitRate(256000);
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
            log.error("변환 실패: {}", m3u8Url, e);
            if (target != null && target.exists() && !target.delete()) {
                log.warn("임시 파일 삭제 실패: {}", target.getAbsolutePath());
            }
            throw new IllegalStateException("m3u8 -> wav 변환 실패", e);
        }
    }
}
