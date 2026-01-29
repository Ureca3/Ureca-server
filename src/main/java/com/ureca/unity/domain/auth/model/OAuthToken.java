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
    private String provider;
    private String accessToken;
    private String refreshToken;
    private LocalDateTime expiresAt;
}
