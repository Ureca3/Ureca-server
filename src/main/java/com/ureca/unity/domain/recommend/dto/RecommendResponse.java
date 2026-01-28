package com.ureca.unity.domain.recommend.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;


@Getter @Setter
public class RecommendResponse {
  private long summaryId;
  private List<RecommendItem> items;
}