package com.ureca.unity.domain.user.model;

import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    private Long id;

    private String provider;
    private String providerId;

    private String email;
    private String name;

    private String role;

    private Instant termsAgreedAt;
    private LocalDateTime deletedAt;
}