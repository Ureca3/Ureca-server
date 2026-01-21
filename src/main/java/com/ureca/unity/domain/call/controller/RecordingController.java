package com.ureca.unity.domain.call.controller;

import com.ureca.unity.domain.call.dto.request.RecordingRequest;
import com.ureca.unity.domain.call.dto.request.RecordingStopRequest;
import com.ureca.unity.domain.call.dto.response.RecordingResponse;
import com.ureca.unity.domain.call.service.AgoraRecordingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recording")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class RecordingController {

    private final AgoraRecordingService recordingService;

    /**
     * 녹음 시작 API
     * acquire와 start를 동시에 진행하여 client에게 sid와 resourceId를 반환합니다.
     */
    @PostMapping("/start")
    public ResponseEntity<RecordingResponse> startRecording(@RequestBody RecordingRequest request) {
        try {
            String resourceId = recordingService.acquire(request.channelName(), request.uid());
            String sid = recordingService.start(resourceId, request.channelName(), request.uid(), request.token());
            return ResponseEntity.ok(new RecordingResponse(resourceId, sid));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 녹음 종료 API
     */
    @PostMapping("/stop")
    public ResponseEntity<String> stopRecording(@RequestBody RecordingStopRequest request) {
        try {
            recordingService.stop(
                    request.resourceId(),
                    request.sid(),
                    request.channelName(),
                    request.uid()
            );
            return ResponseEntity.ok("Recording stopped successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to stop recording: " + e.getMessage());
        }
    }
}