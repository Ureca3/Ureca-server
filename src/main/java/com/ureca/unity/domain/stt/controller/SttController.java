package com.ureca.unity.domain.stt.controller;

import com.ureca.unity.domain.stt.model.SttJob;
import com.ureca.unity.domain.stt.service.SttService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/counselings")
public class SttController {

    private final SttService sttService;

    @GetMapping("/{counselingId}/stt")
    public SttJob get(@PathVariable Long counselingId) {
        return sttService.getStt(counselingId);
    }


}
