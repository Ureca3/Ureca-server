package com.ureca.unity.domain.auth.service;

import com.ureca.unity.domain.auth.mapper.RefreshTokenMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class LogoutServiceImpl implements LogoutService {

    private final RefreshTokenMapper refreshTokenMapper;

    @Override
    public void logout(String refreshToken) {

// refreshToken 없어도 로그아웃은 성공
        if (refreshToken == null || refreshToken.isBlank()) {
            log.info("Logout: no refreshToken provided");
            return;
        }

        log.info("Logout: delete refreshToken from DB");
        refreshTokenMapper.deleteByToken(refreshToken);
    }
}
