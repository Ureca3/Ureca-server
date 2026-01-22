package com.ureca.unity.domain.summary.controller;

import com.ureca.unity.domain.summary.dto.request.SummaryRequest;
import com.ureca.unity.domain.summary.dto.response.SummaryResponse;
import com.ureca.unity.domain.summary.service.SummaryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/summaries")
public class SummaryController {

    private final SummaryService summaryService;

    @PostMapping
    public SummaryResponse create(@Valid @RequestBody SummaryRequest request) {
        return summaryService.createSummary(
                request.getSttJobId(),
                request.getCounselingId(),
                request.getUserId(),
                request.getCounselingText()
        );
    }

    @PatchMapping("/{summaryId}/bookmark")
    public void toggleBookmark(@PathVariable Long summaryId) {
        summaryService.toggleBookmark(summaryId);
    }
}
