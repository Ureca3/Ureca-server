package com.ureca.unity.domain.summary.controller;

import com.ureca.unity.domain.summary.dto.response.SummaryDetailResponse;
import com.ureca.unity.domain.summary.dto.response.SummaryListResponse;
import com.ureca.unity.domain.summary.service.SummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/summaries")
@RequiredArgsConstructor
public class SummaryController {

    private final SummaryService summaryService;

    @GetMapping
    public List<SummaryListResponse> getMySummaries(
            @RequestParam Long userId
    ) {
        return summaryService.getMySummaries(userId);
    }

    @GetMapping("/bookmarks")
    public List<SummaryListResponse> getBookmarkedSummaries(
            @RequestParam Long userId
    ) {
        return summaryService.getBookmarkedSummaries(userId);
    }

    @GetMapping("/{summaryId}")
    public SummaryDetailResponse getSummaryDetail(
            @PathVariable Long summaryId
    ) {
        return summaryService.getSummaryDetail(summaryId);
    }

    @PatchMapping("/{summaryId}/bookmark")
    public void toggleBookmark(
            @PathVariable Long summaryId
    ) {
        summaryService.toggleBookmark(summaryId);
    }
}
