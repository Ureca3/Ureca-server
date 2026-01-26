package com.ureca.unity.domain.stt.model;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CounselingResult {

    private Long counselingResultId;
    private Long userId;
    private Long counselorId;
    private String counselingType;
    private String texts;
    private String status;
}
