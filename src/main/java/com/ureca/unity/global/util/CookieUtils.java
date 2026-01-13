package com.ureca.unity.global.util;

import jakarta.servlet.http.Cookie;

public class CookieUtils {

    // private CookieUtils() {}
    private static final String REFRESH_TOKEN = "refreshToken";
    private static final String PATH = "/api/auth";

    public static Cookie createRefreshTokenCookie(
            String token,
            long maxAgeSeconds,
            boolean secure
    ) {
        Cookie cookie = new Cookie(REFRESH_TOKEN, token);
        cookie.setHttpOnly(true);
        cookie.setSecure(secure);
        cookie.setPath(PATH);
        cookie.setMaxAge((int) maxAgeSeconds);
        cookie.setAttribute("SameSite", "Strict");
        return cookie;
    }

    public static Cookie deleteRefreshTokenCookie(boolean secure) {
        Cookie cookie = new Cookie(REFRESH_TOKEN, null);
        cookie.setHttpOnly(true);
        cookie.setSecure(secure);
        cookie.setPath(PATH);
        cookie.setMaxAge(0);
        cookie.setAttribute("SameSite", "Strict");
        return cookie;
    }
}
