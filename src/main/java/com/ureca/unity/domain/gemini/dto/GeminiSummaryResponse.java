package com.ureca.unity.domain.gemini.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class GeminiSummaryResponse {
    private String title;
    private String subject;
    private List<String> keywords;
    private List<String> points;
}
