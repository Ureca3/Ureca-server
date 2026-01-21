package com.ureca.unity.domain.summary.model;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class SummaryModel {
    private Long summaryId;
    private Long sttJodId;
    private Long counselingId;
    private Long userId;

    private String title;
    private String subject;
    private List<String> keywords;
    private List<String> points;

    private boolean isBookmarked;
    private LocalDateTime createdAt;
}
