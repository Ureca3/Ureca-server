package com.ureca.unity.domain.auth.model;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OAuthToken {
    private Long id;
    private Long userId;
    private String provider;      // google / naver / kakao
    private String accessToken;
    private String refreshToken;  // nullable
    private LocalDateTime expiresAt;    // nullable
}
