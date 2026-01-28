package com.ureca.unity.domain.recommend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RecommendRequest {
  private long summaryId;
  private int k = 5; //기본값
}