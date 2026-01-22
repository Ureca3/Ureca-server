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
            Long sttJobId,
            Long counselingId,
            Long userId,
            String counselingText
    ) {

        GeminiSummaryResponse geminiResult =
                geminiSummaryService.summarize(counselingText);

        List<String> keywords =
                geminiResult.getKeywords() != null
                        ? geminiResult.getKeywords()
                        : Collections.emptyList();

        List<String> points =
                geminiResult.getPoints() != null
                        ? geminiResult.getPoints()
                        : Collections.emptyList();

        try {
            String keywordsJson =
                    objectMapper.writeValueAsString(keywords);
            String pointsJson =
                    objectMapper.writeValueAsString(points);

            summaryMapper.insertSummary(
                    sttJobId,
                    counselingId,
                    userId,
                    geminiResult.getTitle(),
                    geminiResult.getSubject(),
                    keywordsJson,
                    pointsJson
            );

        } catch (Exception e) {
            throw new IllegalStateException("Summary 저장 실패", e);
        }

        return new SummaryResponse(
                geminiResult.getTitle(),
                geminiResult.getSubject(),
                keywords,
                points
        );
    }
}
