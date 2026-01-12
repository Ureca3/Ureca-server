package com.ureca.unity.global.util;

import jakarta.servlet.http.Cookie;

public class CookieUtils {

    private CookieUtils() {}

    public static Cookie createRefreshTokenCookie(
            String token,
            long maxAgeSeconds,
            boolean secure
    ) {
        Cookie cookie = new Cookie("refreshToken", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(secure);
        cookie.setPath("/api/auth");
        cookie.setMaxAge((int) maxAgeSeconds);
        cookie.setAttribute("SameSite", "Strict");
        return cookie;
    }

    public static Cookie deleteRefreshTokenCookie(boolean secure) {
        Cookie cookie = new Cookie("refreshToken", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(secure);
        cookie.setPath("/api/auth");
        cookie.setMaxAge(0);
        cookie.setAttribute("SameSite", "Strict");
        return cookie;
    }
}
