package com.ureca.unity.domain.recommend.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;
import com.ureca.unity.domain.recommend.dto.RecommendRequest;
import com.ureca.unity.domain.recommend.dto.RecommendResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RecommendClient {

  private final RestTemplate restTemplate;

  @Value("${python.recommend.base-url:http://localhost:8000}")
  private String baseUrl;

  public RecommendResponse recommend(RecommendRequest req) {
    return restTemplate.postForObject(
      baseUrl + "/api/v1/summary/" + req.getSummaryId() + "/recommend?k=" + req.getK(),
      null,
      RecommendResponse.class
    );
  }
}

