package com.ureca.unity.domain.recommend.mapper;

import com.ureca.unity.domain.recommend.dto.RecommendItem;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RecommendMapper {
  void deleteItems(@Param("summaryId") long summaryId);
  void insertItems(@Param("summaryId") long summaryId, @Param("items") List<RecommendItem> items);
  List<RecommendItem> selectBySummaryId(@Param("summaryId") long summaryId);
  List<RecommendItem> selectRandomByCategory(@Param("categoryId") int categoryId);
}
