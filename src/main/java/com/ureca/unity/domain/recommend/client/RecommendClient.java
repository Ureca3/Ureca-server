package com.ureca.unity.domain.recommend.client;

import com.ureca.unity.domain.recommend.dto.RecommendResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class RecommendClient {

    private final RestTemplate restTemplate;

    @Value("${python.recommend.base-url:http://localhost:8000}")
    private String baseUrl;

    public RecommendResponse recommend(long summaryId, int k) {
        String url = UriComponentsBuilder
                .fromHttpUrl(baseUrl)
                .path("/api/v1/summary/{summaryId}/recommend")
                .queryParam("k", k)
                .buildAndExpand(summaryId)
                .toUriString();

        return restTemplate.postForObject(
                url,
                null,
                RecommendResponse.class
        );
    }
}
