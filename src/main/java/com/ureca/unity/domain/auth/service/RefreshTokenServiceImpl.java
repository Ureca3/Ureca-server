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

        String refreshToken = extractRefreshToken(request);

        Long jwtUserId;
        try {
            jwtUserId = jwtProvider.getUserId(refreshToken, "refresh");
        } catch (JwtException | IllegalArgumentException | CustomException e) {
            throw new CustomException(ErrorCode.REFRESH_TOKEN_INVALID);
        }

        RefreshToken savedToken = refreshTokenMapper.findByToken(refreshToken)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.REFRESH_TOKEN_INVALID)
                );

        if (savedToken.getExpiresAt() == null) {
            throw new CustomException(ErrorCode.REFRESH_TOKEN_INVALID);
        }

        if (savedToken.getExpiresAt().isBefore(Instant.now())) {
            refreshTokenMapper.deleteByToken(refreshToken);
            throw new CustomException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        }

        Long userId = savedToken.getUserId();

        if (!userId.equals(jwtUserId)) {
            throw new CustomException(ErrorCode.REFRESH_TOKEN_INVALID);
        }

        int deletedCount = refreshTokenMapper.deleteByToken(refreshToken);
        if (deletedCount == 0) {
            throw new CustomException(ErrorCode.REFRESH_TOKEN_INVALID);
        }

        TokenResponse accessToken = jwtIssuer.issueAccessToken(userId);
        
        String newRefreshToken = jwtIssuer.issueRefreshToken(userId);

        saveRefreshToken(userId, newRefreshToken);

        response.addCookie(CookieUtils.createRefreshTokenCookie(
                newRefreshToken,
                jwtProperties.refreshExpirationSeconds(),
                cookieSecure
                )
        );

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
