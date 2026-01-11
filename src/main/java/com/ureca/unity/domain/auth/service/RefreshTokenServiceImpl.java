package com.ureca.unity.domain.auth.service;

import com.ureca.unity.domain.auth.dto.TokenResponse;
import com.ureca.unity.domain.auth.model.RefreshToken;
import com.ureca.unity.domain.auth.mapper.RefreshTokenMapper;
import com.ureca.unity.global.security.JwtIssuer;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenMapper refreshTokenMapper;
    private final JwtIssuer jwtIssuer;

    @Override
    public TokenResponse refreshAccessToken(HttpServletRequest request) {

        // 1. Cookie에서 refreshToken 추출
        String refreshToken = extractRefreshToken(request);

        // 2. DB 조회
        RefreshToken savedToken = refreshTokenMapper.findByToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        // 3. 만료 체크
        if (savedToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Refresh token expired");
        }

        // 4. 새 AccessToken 발급
        return jwtIssuer.issueAccessToken(savedToken.getUserId());
    }

    private String extractRefreshToken(HttpServletRequest request) {
        if (request.getCookies() == null) {
            throw new IllegalArgumentException("Refresh token cookie missing");
        }

        for (Cookie cookie : request.getCookies()) {
            if ("refreshToken".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        throw new IllegalArgumentException("Refresh token cookie missing");
    }
}
