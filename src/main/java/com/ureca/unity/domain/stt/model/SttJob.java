package com.ureca.unity.domain.stt.model;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SttJob {

    private Long sttJobId;
    private Long counselingId;
    private Long userId;

    private String summaryType; // CALL / KEYWORD
    private String status;      // LOADING(PROGRESS) / SUCCESS / FAIL

    private String summaryText; // STT or LLM result
    private String keywords;    // JSON 문자열로 처리 (임시)

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
