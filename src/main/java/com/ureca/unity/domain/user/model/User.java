package com.ureca.unity.domain.user.model;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    private Long id;

    private String provider;      // google / naver / kakao
    private String providerId;    // OAuth 제공자 고유 ID

    private String email;         // nullable
    private String name;

    private String role;          // ROLE_USER
}