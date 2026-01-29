package com.ureca.unity.domain.recommend.controller;

import com.ureca.unity.domain.recommend.dto.RecommendResponse;
import com.ureca.unity.domain.recommend.service.RecommendService;
import com.ureca.unity.global.exception.CustomException;
import com.ureca.unity.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/recommend")
@RequiredArgsConstructor
public class RecommendController {

  private static final Logger log = LoggerFactory.getLogger(RecommendController.class);

  private final RecommendService recommendService;

  // 요약 추천 생성 (FastAPI 호출 + DB 저장)
  @PostMapping("/{summaryId}/generate")
  public RecommendResponse generate(@PathVariable long summaryId) {
    return recommendService.generateAndSave(summaryId);
  }

  // 요약 추천 조회 (DB 조회만)
  @GetMapping("/{summaryId}")
  public RecommendResponse get(@PathVariable long summaryId) {
    try {
      return recommendService.getFromDb(summaryId);
    } catch (CustomException e) {
      log.error("get failed for summaryId={}", summaryId, e);
      throw e;
    } catch (Exception e) {
      log.error("unexpected error for summaryId={}", summaryId, e);
      throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
    }
  }

  // 전체 추천 조회
  @GetMapping("/me")
  public RecommendResponse getAll() {
    return recommendService.getRandomByCategory();
  }
}

