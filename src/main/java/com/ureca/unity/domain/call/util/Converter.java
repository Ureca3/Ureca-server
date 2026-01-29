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
            target = File.createTempFile("recording_", ".wav");

            // 오디오 설정
            AudioAttributes audio = new AudioAttributes();
            audio.setSamplingRate(16000);
            audio.setBitRate(256000);
            audio.setChannels(1);

            EncodingAttributes attrs = new EncodingAttributes();
            attrs.setOutputFormat("wav");
            attrs.setAudioAttributes(audio);

            Encoder encoder = new Encoder();
            encoder.encode(new MultimediaObject(new URL(m3u8Url)), target, attrs);

            log.info("[WAV] 변환 성공: {}", target.getAbsolutePath());
            return target;

        } catch (Exception e) {
            log.error("변환 실패: {}", m3u8Url, e);
            if (target != null && target.exists() && !target.delete()) {
                log.warn("임시 파일 삭제 실패: {}", target.getAbsolutePath());
            }
            throw new IllegalStateException("[WAV] m3u8 -> wav 변환 실패", e);
        }
    }
}
