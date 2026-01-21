package com.ureca.unity.domain.summary.service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ureca.unity.domain.gemini.dto.GeminiSummaryResponse;
import com.ureca.unity.domain.gemini.service.GeminiSummaryService;
import com.ureca.unity.domain.summary.dto.response.SummaryResponse;
import com.ureca.unity.domain.summary.mapper.SummaryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SummaryService {

    private final GeminiSummaryService geminiSummaryService;
    private final SummaryMapper summaryMapper;
    private final ObjectMapper objectMapper;

    public SummaryResponse createSummary(
            Long sttJobId,
            Long counselingId,
            Long userId,
            String counselingText
    ) {

        // 1️⃣ Gemini 요약 생성
        GeminiSummaryResponse geminiResult =
                geminiSummaryService.summarize(counselingText);

        try {
            // 2️⃣ JSON 직렬화
            String keywordsJson =
                    objectMapper.writeValueAsString(geminiResult.getKeywords());
            String pointsJson =
                    objectMapper.writeValueAsString(geminiResult.getPoints());

            // 3️⃣ DB 저장
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

        // 4️⃣ 응답 반환
        return new SummaryResponse(
                geminiResult.getTitle(),
                geminiResult.getSubject(),
                geminiResult.getKeywords(),
                geminiResult.getPoints()
        );
    }
}
