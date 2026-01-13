package com.ureca.unity.domain.auth.service;

import com.ureca.unity.domain.auth.constant.JwtProperties;
import com.ureca.unity.domain.auth.dto.TokenResponse;
import com.ureca.unity.domain.auth.model.RefreshToken;
import com.ureca.unity.domain.auth.mapper.RefreshTokenMapper;
import com.ureca.unity.global.exception.CustomException;
import com.ureca.unity.global.exception.ErrorCode;
import com.ureca.unity.global.security.JwtIssuer;
import com.ureca.unity.global.util.CookieUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenMapper refreshTokenMapper;
    private final JwtIssuer jwtIssuer;
    private final JwtProperties jwtProperties;

    @Value("${cookie.secure:false}")
    private boolean cookieSecure;

    @Override
    public void saveRefreshToken(Long userId, String refreshToken) {
        refreshTokenMapper.deleteByUserId(userId);

        refreshTokenMapper.insert(
                RefreshToken.builder()
                        .userId(userId)
                        .token(refreshToken)
                        .expiresAt(
                                LocalDateTime.now()
                                        .plusSeconds(jwtProperties.refreshExpirationSeconds())
                        )
                        .build()
        );
    }

    @Transactional
    @Override
    public TokenResponse refreshAccessToken(HttpServletRequest request,
                                            HttpServletResponse response) {

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

        Long userId = savedToken.getUserId();

        // 4. 기존 refreshToken 폐기 (Rotation)
        refreshTokenMapper.deleteByToken(refreshToken);

        // 5. 새 Access + Refresh 발급
        TokenResponse accessToken = jwtIssuer.issueAccessToken(userId);
        
        // 6. 새 refreshToken 발급
        String newRefreshToken = jwtIssuer.issueRefreshToken(userId);

        // 7. DB 저장

        saveRefreshToken(userId, newRefreshToken);

        response.addCookie(CookieUtils.createRefreshTokenCookie(
                newRefreshToken,
                jwtProperties.refreshExpirationSeconds(),
                cookieSecure
                )
        );

        // 8. accessToken만 반환
        return accessToken;
    }

    private String extractRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            throw new CustomException(ErrorCode.REFRESH_TOKEN_MISSING);
        }

        for (Cookie cookie : cookies) {
            if (CookieUtils.REFRESH_TOKEN.equals(cookie.getName())) {
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
