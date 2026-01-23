package com.ureca.unity.domain.summary.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SummaryRequest {

    @NotNull
    private Long counselingResultId;

    @NotNull
    private Long userId;

    @NotBlank
    private String counselingText;
}
