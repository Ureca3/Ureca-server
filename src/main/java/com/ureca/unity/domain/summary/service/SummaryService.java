package com.ureca.unity.domain.summary.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ureca.unity.domain.gemini.dto.GeminiSummaryResponse;
import com.ureca.unity.domain.gemini.service.GeminiSummaryService;
import com.ureca.unity.domain.summary.dto.response.SummaryResponse;
import com.ureca.unity.domain.summary.mapper.SummaryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SummaryService {

    private final GeminiSummaryService geminiSummaryService;
    private final SummaryMapper summaryMapper;
    private final ObjectMapper objectMapper;

    @Transactional
    public SummaryResponse createSummary(
            Long counselingResultId,
            Long userId,
            String counselingText
    ) {
        // 1. summary 먼저 생성 (loading)
        summaryMapper.insertSummary(counselingResultId, userId);

        Long summaryId =
                summaryMapper.findLatestSummaryId(userId, counselingResultId);

        try {
            GeminiSummaryResponse gemini =
                    geminiSummaryService.summarize(counselingText);

            List<String> keywords =
                    gemini.getKeywords() != null
                            ? gemini.getKeywords()
                            : Collections.emptyList();

            List<String> points =
                    gemini.getPoints() != null
                            ? gemini.getPoints()
                            : Collections.emptyList();

            summaryMapper.updateSummaryResult(
                    summaryId,
                    gemini.getTitle(),
                    gemini.getSubject(),
                    objectMapper.writeValueAsString(keywords),
                    objectMapper.writeValueAsString(points)
            );

            return new SummaryResponse(
                    gemini.getTitle(),
                    gemini.getSubject(),
                    keywords,
                    points
            );

        } catch (Exception e) {
            summaryMapper.updateStatus(summaryId, "FAIL");
            throw new IllegalStateException("요약 생성 실패", e);
        }
    }

    @Transactional
    public void toggleBookmark(Long summaryId) {
        Boolean isBookmarked =
                summaryMapper.findBookmarkStatus(summaryId);

        if (isBookmarked == null) {
            throw new IllegalArgumentException("Summary not found");
        }

        summaryMapper.updateBookmark(summaryId, !isBookmarked);
    }
}
