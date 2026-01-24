package com.ureca.unity.domain.stt.controller;

import com.ureca.unity.domain.stt.model.SttJob;
import com.ureca.unity.domain.stt.service.SttService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "3. STT",
        description = "음성 인식(STT) API"
)
@RestController
@RequiredArgsConstructor
@RequestMapping("/counselings")
public class SttController {

    private final SttService sttService;

    @PostMapping("/{counselingId}/stt")
    public SttJob start(@PathVariable Long counselingId) {
        return sttService.startStt(counselingId);
    }

    @GetMapping("/{counselingId}/stt")
    public SttJob get(@PathVariable Long counselingId) {
        return sttService.getStt(counselingId);
    }
}
