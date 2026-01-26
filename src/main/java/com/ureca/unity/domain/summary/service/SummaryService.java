package com.ureca.unity.domain.summary.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ureca.unity.domain.gemini.dto.GeminiSummaryResponse;
import com.ureca.unity.domain.gemini.service.GeminiSummaryService;
import com.ureca.unity.domain.summary.dto.response.SummaryDetailResponse;
import com.ureca.unity.domain.summary.dto.response.SummaryListResponse;
import com.ureca.unity.domain.summary.dto.response.SummaryResponse;
import com.ureca.unity.domain.summary.mapper.SummaryMapper;
import com.ureca.unity.domain.summary.model.SummaryModel;
import com.ureca.unity.global.exception.CustomException;
import com.ureca.unity.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SummaryService {

    private final GeminiSummaryService geminiSummaryService;
    private final SummaryMapper summaryMapper;
    private final ObjectMapper objectMapper;

    public void createSummary(
            Long counselingResultId,
            Long userId,
            String counselingText
    ) {
        if(userId==null) throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        summaryMapper.insertSummary(counselingResultId, userId);

        Long summaryId =
                summaryMapper.findLatestSummaryId(userId, counselingResultId);

        try {
            GeminiSummaryResponse gemini =
                    geminiSummaryService.summarize(counselingText);

            if (gemini.getKeywords() == null || gemini.getKeywords().isEmpty()
                    || gemini.getPoints() == null || gemini.getPoints().isEmpty()){
                summaryMapper.updateStatus(summaryId, "FAIL");
                throw new RuntimeException("Gemini 결과 누락");
            }

            List<String> keywords = gemini.getKeywords();
            List<String> points = gemini.getPoints();

            summaryMapper.updateSummaryResult(
                    summaryId,
                    gemini.getTitle(),
                    gemini.getSubject(),
                    objectMapper.writeValueAsString(keywords),
                    objectMapper.writeValueAsString(points)
            );

            summaryMapper.updateStatus(summaryId, "SUCCESS");

            new SummaryResponse(
                    gemini.getTitle(),
                    gemini.getSubject(),
                    keywords,
                    points
            );

        } catch (Exception e) {
            summaryMapper.updateStatus(summaryId, "FAIL");
            throw new IllegalStateException(e);
        }
    }

    @Transactional(readOnly = true)
    public List<SummaryListResponse> getMySummaries(Long userId) {
        return summaryMapper.findByUserId(userId).stream()
                .map(summary -> {
                    try {
                        List<String> keywords =
                                summary.getKeywords() != null
                                        ? objectMapper.readValue(
                                        summary.getKeywords(),
                                        new TypeReference<List<String>>() {})
                                        : List.of();

                        return new SummaryListResponse(
                                summary.getSummaryId(),
                                summary.getTitle(),
                                summary.getStatus(),
                                keywords,
                                summary.getCreatedAt()
                        );
                    } catch (Exception e) {
                        throw new IllegalStateException(e);
                    }
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public SummaryDetailResponse getSummaryDetail(Long summaryId) {
        SummaryModel summary = summaryMapper.findById(summaryId);

        if (summary == null) {
            return null;
        }

        try {
            List<String> keywords =
                    summary.getKeywords() != null
                            ? objectMapper.readValue(
                            summary.getKeywords(),
                            new TypeReference<List<String>>() {})
                            : List.of();

            List<String> points =
                    summary.getPoints() != null
                            ? objectMapper.readValue(
                            summary.getPoints(),
                            new TypeReference<List<String>>() {})
                            : List.of();

            return new SummaryDetailResponse(
                    summary.getSummaryId(),
                    summary.getTitle(),
                    summary.getSubject(),
                    keywords,
                    points,
                    summary.getStatus(),
                    summary.getIsBookmarked(),
                    summary.getCreatedAt()
            );

        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Transactional
    public void toggleBookmark(Long summaryId) {
        Boolean isBookmarked =
                summaryMapper.findBookmarkStatus(summaryId);

        if (isBookmarked == null) {
            throw new IllegalArgumentException();
        }

        summaryMapper.updateBookmark(summaryId, !isBookmarked);
    }
}
