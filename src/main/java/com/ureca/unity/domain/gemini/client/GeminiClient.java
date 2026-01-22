package com.ureca.unity.domain.gemini.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class GeminiClient {

    private final RestClient geminiRestClient;
    private final ObjectMapper objectMapper;

    public String generateContent(String prompt) {

        try {

            Map<String, Object> body = Map.of(
                    "contents", List.of(
                            Map.of(
                                    "parts", List.of(
                                            Map.of("text", prompt)
                                    )
                            )
                    )
            );

            return geminiRestClient.post()
                    .uri("/v1beta/models/gemini-2.5-flash:generateContent")
                    .body(objectMapper.writeValueAsString(body))
                    .retrieve()
                    .body(String.class);

        } catch (Exception e) {
            throw new IllegalStateException("Gemini API 요청 실패", e);
        }
    }
}
