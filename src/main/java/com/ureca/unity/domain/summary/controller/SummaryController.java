package com.ureca.unity.domain.summary.controller;

import com.ureca.unity.domain.summary.dto.request.SummaryRequest;
import com.ureca.unity.domain.summary.dto.response.SummaryResponse;
import com.ureca.unity.domain.summary.service.SummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/summaries")
public class SummaryController {

    private final SummaryService summaryService;

    @PostMapping
    public SummaryResponse create(@RequestBody SummaryRequest request) {

        Long counselingId = 2L;

        return summaryService.createSummary(
                request.getSttJobId(),
                request.getCounselingId(),
                request.getUserId(),
                request.getCounselingText()
        );
    }
}
