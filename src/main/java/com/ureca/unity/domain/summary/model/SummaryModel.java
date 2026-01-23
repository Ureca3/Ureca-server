package com.ureca.unity.domain.summary.model;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class SummaryModel {

    private Long summaryId;
    private Long counselingResultId;
    private Long userId;

    private String title;
    private String subject;

    private String keywords;   // JSON
    private String points;    // JSON

    private boolean isBookmarked;
    private String status;
    private LocalDateTime createdAt;
}
