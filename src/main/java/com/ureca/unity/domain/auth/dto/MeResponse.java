package com.ureca.unity.domain.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class MeResponse {
    private final Long id;
    private final String email;
    private final String name;
    private final String role;
    private final String provider;

    private final boolean termsAgreed;
    private final Instant termsAgreedAt;
}
