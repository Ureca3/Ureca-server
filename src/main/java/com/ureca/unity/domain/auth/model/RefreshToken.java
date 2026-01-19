package com.ureca.unity.domain.auth.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RefreshToken {

    private Long id;
    private Long userId;
    private String token;
    private Instant expiresAt;
}
