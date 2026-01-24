package com.ureca.unity.domain.summary.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class SummaryResponse {

    private String title;
    private String subject;
    private List<String> keywords;
    private List<String> points;
}
