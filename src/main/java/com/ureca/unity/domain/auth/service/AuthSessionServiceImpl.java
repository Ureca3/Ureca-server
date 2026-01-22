package com.ureca.unity.domain.auth.service;

import com.ureca.unity.domain.auth.dto.MeResponse;
import com.ureca.unity.domain.auth.mapper.RefreshTokenMapper;
import com.ureca.unity.domain.auth.model.RefreshToken;
import com.ureca.unity.domain.user.mapper.UserMapper;
import com.ureca.unity.domain.user.model.User;
import com.ureca.unity.global.exception.CustomException;
import com.ureca.unity.global.exception.ErrorCode;
import com.ureca.unity.global.security.JwtProvider;
import com.ureca.unity.global.util.CookieUtils;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthSessionServiceImpl implements AuthSessionService {

    private final RefreshTokenMapper refreshTokenMapper;
    private final JwtProvider jwtProvider;
    private final UserMapper userMapper;

    @Override
    public MeResponse getMe(HttpServletRequest request) {
        String refreshToken = extractRefreshToken(request);

        // (1) refresh JWT 서명 + type 검증
        Long jwtUserId;
        try {
            jwtUserId = jwtProvider.getUserId(refreshToken, "refresh");
        } catch (JwtException | IllegalArgumentException | CustomException e) {
            throw new CustomException(ErrorCode.REFRESH_TOKEN_INVALID);
        }

        // (2) DB에 저장된 토큰인지 확인
        RefreshToken saved = refreshTokenMapper.findByToken(refreshToken)
                .orElseThrow(() -> new CustomException(ErrorCode.REFRESH_TOKEN_INVALID));

        // (3) 만료 체크
        if (saved.getExpiresAt() == null || saved.getExpiresAt().isBefore(Instant.now())) {
            refreshTokenMapper.deleteByToken(refreshToken);
            throw new CustomException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        }

        // (4) 토큰의 userId 일치 체크
        if (!saved.getUserId().equals(jwtUserId)) {
            throw new CustomException(ErrorCode.REFRESH_TOKEN_INVALID);
        }

        // (5) 유저 조회
        User user = userMapper.findById(saved.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return new MeResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole(),
                user.getProvider()
        );
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
