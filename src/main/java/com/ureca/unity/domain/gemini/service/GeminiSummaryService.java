package com.ureca.unity.domain.gemini.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ureca.unity.domain.gemini.client.GeminiClient;
import com.ureca.unity.domain.gemini.dto.GeminiSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GeminiSummaryService {

    private final GeminiClient geminiClient;
    private final ObjectMapper objectMapper;

    public GeminiSummaryResponse summarize(String counselingText) {

        String prompt = """
        너는 통신 상담 내용을 요약하는 시스템이다.

        아래 상담 내용을 분석하여 반드시 다음 JSON 형식으로만 출력하라.

        {
          "title": string,
          "subject": string,
          "keywords": string[],
          "points": string[]
        }

        조건:
        - title: 상담 내용을 대표하는 요약 제목
        - subject: 요약의 핵심 주제 한 문장
        - keywords: 상담의 핵심 키워드 2~4개
        - points:
          - 핵심 요약 포인트
          - 최대 3개
          - 각 항목은 한 문장
          - 가격은 숫자로 명시
        - 상담 내용에 없는 정보는 생성하지 말 것
        - JSON 외의 텍스트는 절대 출력하지 말 것

        상담 내용:
        %s
        """.formatted(counselingText);

        String rawResponse = geminiClient.generateContent(prompt);

        try {
            JsonNode root = objectMapper.readTree(rawResponse);

            String text = root
                    .path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();

            // ✅ Markdown 코드블록 제거
            text = text
                    .replaceAll("```json", "")
                    .replaceAll("```", "")
                    .trim();

            return objectMapper.readValue(text, GeminiSummaryResponse.class);

        } catch (Exception e) {
            throw new IllegalStateException("Gemini 요약 파싱 실패", e);
        }

    }
}
