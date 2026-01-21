package com.ureca.unity.domain.summary.dto.request;

import lombok.Getter;

@Getter
public class SummaryRequest {
    private Long sttJobId;
    private Long counselingId;
    private Long userId;
    private String counselingText;
}
