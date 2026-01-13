package com.ureca.unity.domain.auth.service;

import com.ureca.unity.domain.auth.constant.JwtProperties;
import com.ureca.unity.domain.auth.dto.TokenResponse;
import com.ureca.unity.domain.auth.model.RefreshToken;
import com.ureca.unity.domain.auth.mapper.RefreshTokenMapper;
import com.ureca.unity.global.exception.CustomException;
import com.ureca.unity.global.exception.ErrorCode;
import com.ureca.unity.global.security.JwtIssuer;
import com.ureca.unity.global.security.JwtProvider;
import com.ureca.unity.global.util.CookieUtils;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenMapper refreshTokenMapper;
    private final JwtIssuer jwtIssuer;
    private final JwtProperties jwtProperties;
    private final JwtProvider jwtProvider;

    @Value("${cookie.secure:false}")
    private boolean cookieSecure;

    @Transactional
    @Override
    public void saveRefreshToken(Long userId, String refreshToken) {
        refreshTokenMapper.deleteByUserId(userId);

        refreshTokenMapper.insert(
                RefreshToken.builder()
                        .userId(userId)
                        .token(refreshToken)
                        .expiresAt(
                                Instant.now()
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

        // 1-1. JWT 서명 검증 (DB 조회 전)
        Long jwtUserId;
        try {
            jwtUserId = jwtProvider.getUserId(refreshToken, "refresh");
        } catch (JwtException | IllegalArgumentException | CustomException e) {
            throw new CustomException(ErrorCode.REFRESH_TOKEN_INVALID);
        }

        // 2. DB 조회
        RefreshToken savedToken = refreshTokenMapper.findByToken(refreshToken)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.REFRESH_TOKEN_INVALID)
                );

        if (savedToken.getExpiresAt() == null) {
            throw new CustomException(ErrorCode.REFRESH_TOKEN_INVALID);
        }

        // 3. 만료 체크
        if (savedToken.getExpiresAt().isBefore(Instant.now())) {
            // 만료된 토큰은 즉시 삭제 후 예외 반환 (rotation 로직 미진입)
            refreshTokenMapper.deleteByToken(refreshToken); // 만료 토큰 정리
            throw new CustomException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        }

        Long userId = savedToken.getUserId();

        if (!userId.equals(jwtUserId)) {
            throw new CustomException(ErrorCode.REFRESH_TOKEN_INVALID);
        }

        // 4. 기존 refreshToken 폐기 (Rotation 로직 진입)
        int deletedCount = refreshTokenMapper.deleteByToken(refreshToken);
        if (deletedCount == 0) {
            throw new CustomException(ErrorCode.REFRESH_TOKEN_INVALID);
        }

        // 5. 새 Access + Refresh 발급
        TokenResponse accessToken = jwtIssuer.issueAccessToken(userId);
        
        // 6. 새 refreshToken 발급
        String newRefreshToken = jwtIssuer.issueRefreshToken(userId);

        // 7. DB 저장
        saveRefreshToken(userId, newRefreshToken);

        // 8. 새 refreshToken 쿠키 설정
        response.addCookie(CookieUtils.createRefreshTokenCookie(
                newRefreshToken,
                jwtProperties.refreshExpirationSeconds(),
                cookieSecure
                )
        );

        // 9. accessToken만 반환
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
