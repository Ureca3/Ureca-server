package com.ureca.unity.domain.recommend.service;

import com.ureca.unity.domain.category.mapper.CategoryMapper;
import com.ureca.unity.domain.recommend.client.RecommendClient;
import com.ureca.unity.domain.recommend.dto.RecommendItem;
import com.ureca.unity.domain.recommend.dto.RecommendResponse;
import com.ureca.unity.domain.recommend.mapper.RecommendMapper;
import com.ureca.unity.global.exception.CustomException;
import com.ureca.unity.global.exception.ErrorCode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.ureca.unity.domain.category.dto.Category;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RecommendService {

  private final RecommendClient recommendClient;
  private final RecommendMapper recommendMapper;
  private final CategoryMapper categoryMapper;
  private static final Logger log = LoggerFactory.getLogger(RecommendService.class);

  // 추천 생성 + 저장
  @Transactional
  public RecommendResponse generateAndSave(long summaryId) {
    RecommendResponse response;

    // FastAPI 호출
    try {
      response = recommendClient.recommend(summaryId, 5);
    } catch (Exception e) {
      throw new CustomException(ErrorCode.FASTAPI_CALL_FAILED);
    }

    if (response == null || response.getItems() == null || response.getItems().isEmpty()) {
      throw new CustomException(ErrorCode.RECOMMEND_EMPTY);
    }

    // DB 삭제
    try {
      recommendMapper.deleteItems(summaryId);
    } catch (Exception e) {
      throw new CustomException(ErrorCode.RECOMMEND_DELETE_FAILED);
    }

    List<RecommendItem> items = response.getItems();
    items.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));
    for (int i = 0; i < items.size(); i++) {
      items.get(i).setRank(i); // 0,1,2,... 순서대로
    }

    // DB 삽입
    try {
      recommendMapper.insertItems(summaryId, items);
    } catch (Exception e) {
      throw new CustomException(ErrorCode.RECOMMEND_INSERT_FAILED);
    }

    return response;
  }

  // DB 조회만
  public RecommendResponse getFromDb(long summaryId) {
    List<RecommendItem> items;
    try {
      items = recommendMapper.selectBySummaryId(summaryId);
      log.debug("DB 조회 결과: {}", items);
    } catch (Exception e) {
      log.error("DB 조회 실패 summaryId={}", summaryId, e);
      throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    if (items == null || items.isEmpty()) {
      throw new CustomException(ErrorCode.RECOMMEND_EMPTY);
    }

    RecommendResponse response = new RecommendResponse();
    response.setSummaryId(summaryId);
    response.setItems(items);
    return response;
  }

  @Transactional(readOnly = true)
  public RecommendResponse getRandomByCategory() {
    List<Category> categories = categoryMapper.selectAll();
    List<RecommendItem> results = new ArrayList<>();

    for (Category c : categories) {
      List<RecommendItem> fallback = recommendMapper.selectRandomByCategory(c.getCategoryId());
      results.addAll(fallback);
    }

    RecommendResponse response = new RecommendResponse();
    response.setSummaryId(0);
    response.setItems(results);
    return response;
  }


//  public List<RecommendItem> getRecommendationsWithFallback(long summaryId) {
//    List<RecommendItem> items = recommendMapper.selectBySummaryId(summaryId);
//
//    // 추천 없으면 빈 리스트 처리
//    if (items == null) {
//      items = new ArrayList<>();
//    }
//
//    // 카테고리별로 그룹화
//    Map<Integer, List<RecommendItem>> byCategory = items.stream()
//      .collect(Collectors.groupingBy(RecommendItem::getCategoryId));
//
//    List<Category> categories = categoryMapper.selectAll();
//    for (Category c : categories) {
//      if (!byCategory.containsKey(c.getCategoryId()) || byCategory.get(c.getCategoryId()).isEmpty()) {
//        // 추천 없으면 fallback
//        List<RecommendItem> fallback = recommendMapper.selectRandomByCategory(c.getCategoryId());
//        items.addAll(fallback);
//      }
//    }
//
//    // score=0, rankNo=0인 fallback도 이미 Mapper에서 설정됨
//    return items;
//  }
}
