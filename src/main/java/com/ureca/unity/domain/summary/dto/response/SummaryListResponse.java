package com.ureca.unity.domain.summary.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record SummaryListResponse(
        Long summaryId,
        String title,
        String status,
        List<String> keywords,
        LocalDateTime createdAt
) {}
