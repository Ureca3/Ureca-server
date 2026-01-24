package com.ureca.unity.domain.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MeResponse {
    private final Long id;
    private final String email;
    private final String name;
    private final String role;
    private final String provider;
}
