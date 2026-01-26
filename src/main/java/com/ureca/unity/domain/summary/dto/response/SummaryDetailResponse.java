package com.ureca.unity.domain.summary.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class SummaryDetailResponse {

    private Long summaryId;
    private String title;
    private String subject;
    private List<String> keywords;
    private List<String> points;
    private String status;
    private Boolean isBookmarked;
    private LocalDateTime createdAt;
}
