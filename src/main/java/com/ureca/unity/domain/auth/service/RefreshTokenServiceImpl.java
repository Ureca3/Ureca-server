package com.ureca.unity.domain.auth.service;

import com.ureca.unity.domain.auth.dto.TokenResponse;
import com.ureca.unity.domain.auth.model.RefreshToken;
import com.ureca.unity.domain.auth.mapper.RefreshTokenMapper;
import com.ureca.unity.global.exception.CustomException;
import com.ureca.unity.global.exception.ErrorCode;
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
                .orElseThrow(() ->
                        new CustomException(ErrorCode.REFRESH_TOKEN_INVALID)
                );

        if (savedToken.getExpiresAt() == null) {
            throw new CustomException(ErrorCode.REFRESH_TOKEN_INVALID);
        }

        // 3. 만료 체크
        if (savedToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshTokenMapper.deleteByToken(refreshToken); // 만료 토큰 정리
            throw new CustomException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        }

        // 4. 새 AccessToken 발급
        return jwtIssuer.issueAccessToken(savedToken.getUserId());
    }

    private String extractRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            throw new CustomException(ErrorCode.REFRESH_TOKEN_MISSING);
        }

        for (Cookie cookie : cookies) {
            if ("refreshToken".equals(cookie.getName())) {
                String value = cookie.getValue();
                if (value == null || value.isBlank()) {
                    throw new CustomException(ErrorCode.REFRESH_TOKEN_MISSING);
                }
                return value;
            }
        }

        throw new CustomException(ErrorCode.REFRESH_TOKEN_MISSING);
    }
}
