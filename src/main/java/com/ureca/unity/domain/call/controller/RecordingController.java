package com.ureca.unity.domain.call.controller;

import com.ureca.unity.domain.call.dto.request.RecordingRequest;
import com.ureca.unity.domain.call.dto.request.RecordingStopRequest;
import com.ureca.unity.domain.call.dto.response.RecordingResponse;
import com.ureca.unity.domain.call.service.RecordingServiceImpl;
import com.ureca.unity.global.exception.CustomException;
import com.ureca.unity.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recording")
@RequiredArgsConstructor
public class RecordingController {

    private final RecordingServiceImpl recordingService;

    @PostMapping("/start")
    public ResponseEntity<RecordingResponse> startRecording(@RequestBody RecordingRequest request) {
        String resourceId = recordingService.acquire(request.channelName(), request.uid());
        String sid = recordingService.start(resourceId, request.channelName(), request.uid(), request.token());
        return ResponseEntity.ok(new RecordingResponse(resourceId, sid));
    }

    @PostMapping("/stop")
    public ResponseEntity<String> stopRecording(@RequestBody RecordingStopRequest request) {
        recordingService.stop(
                request.resourceId(),
                request.sid(),
                request.channelName(),
                request.uid(),
                request.userId()
        );
        return ResponseEntity.ok("Recording stopped successfully");
    }
}