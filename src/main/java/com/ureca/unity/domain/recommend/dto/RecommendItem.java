package com.ureca.unity.domain.recommend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RecommendItem {
  private long productId;
  private int categoryId;
  private double score;
  private int rankNo;
  private String name;
  private Double price;
  private String img;
  private String link;
}