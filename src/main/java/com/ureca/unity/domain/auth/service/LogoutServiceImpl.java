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
    public void logout(HttpServletRequest request, HttpServletResponse response) {

        String refreshToken = null;

        // 1. 쿠키에서 refreshToken 추출 (없어도 OK)
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                }
            }
        }

        // 2. DB 삭제 (있으면 삭제, 없으면 무시)
        if (refreshToken != null && !refreshToken.isBlank()) {
            log.info("Logout: delete refreshToken from DB");
            refreshTokenMapper.deleteByToken(refreshToken);
        } else {
            log.info("Logout: no refreshToken cookie");
        }

        // 3. 쿠키 삭제
        Cookie deleteCookie = new Cookie("refreshToken", null);
        deleteCookie.setHttpOnly(true);
        deleteCookie.setSecure(false);
        deleteCookie.setPath("/api/auth");
        deleteCookie.setMaxAge(0);

        response.addCookie(deleteCookie);
    }
}
