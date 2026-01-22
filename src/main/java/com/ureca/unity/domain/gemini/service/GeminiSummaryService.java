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
너는 통신 상담 내용을 분석하여,
'고객이 왜 상담을 했고 무엇을 판단해야 하는지'가 명확히 드러나는 요약을 생성하는 시스템이다.

상담 내용은 통화 녹음을 STT로 변환한 텍스트이므로,
오타, 잘린 문장, 잘못 인식된 단어가 포함될 수 있다.

아래 규칙에 따라 내용을 해석·보정한 뒤,
반드시 다음 JSON 형식으로만 출력하라.

{
  "title": string,
  "subject": string,
  "keywords": string[],
  "points": string[]
}

해석 원칙 (매우 중요):
- 명백한 STT 오류(오타, 조사 누락, 반복어)는 **의미가 명확한 경우에만** 자연스럽게 보정할 수 있다
- 문장이 불완전하더라도 **상담 맥락이 분명하면 요지를 추출**한다
- 단, 상담 내용에 직접 드러나지 않은 정보는 절대 보완·추측하지 말 것
- 의미가 불명확한 부분은 과감히 제외하고, 확실한 정보만 사용

작성 원칙:
- 단순 요약이 아니라 **중요도와 판단 기준 중심**으로 정리할 것
- 고객의 요구, 고민, 제약 조건이 드러나야 함
- 상담사가 강조한 핵심 조건(요금, 약정, 할인, 변경 가능 여부)을 우선 반영

항목별 규칙:

1. title
- 상담의 핵심 결정 또는 갈등 포인트를 드러내는 제목
- 한 문장, 과장 금지
- STT 표현을 그대로 쓰기보다 의미 기준으로 정리

2. subject
- 상담의 본질을 설명하는 한 문장
- 고객 상황 + 상담 목적이 함께 나타나야 함
- 말이 어색한 구어체는 자연스러운 문장으로 정리 가능

3. keywords
- 상담을 관통하는 핵심 키워드 2~4개
- STT 오류로 보이는 단어는 의미 기준으로 정규화 가능
- 단, 상담에서 실제로 언급된 개념만 사용

4. points
- 상담의 핵심 결론만 매우 짧게 요약
- 최대 3개
- 각 항목은 20자 내외의 짧은 문장
- 이유·설명·조건은 포함하지 말 것
- UI 카드에 바로 노출되는 문장이라고 가정할 것
- 가격은 숫자로만 간결히 표기
- 불확실한 내용은 포함하지 말 것

제약 사항:
- 상담 내용에 없는 정보는 생성하지 말 것
- 추측, 일반론, 홍보성 문구 금지
- JSON 외의 텍스트는 절대 출력하지 말 것

상담 내용:
%s
"""
.formatted(counselingText);

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
