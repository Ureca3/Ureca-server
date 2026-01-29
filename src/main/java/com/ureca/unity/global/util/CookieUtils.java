package com.ureca.unity.global.util;

import jakarta.servlet.http.Cookie;

public class CookieUtils {

    private CookieUtils() {}
    public static final String REFRESH_TOKEN = "refreshToken";
    private static final String PATH = "/";

    public static Cookie createRefreshTokenCookie(
            String token,
            long maxAgeSeconds,
            boolean secure
    ) {
        if (maxAgeSeconds > Integer.MAX_VALUE) {
            maxAgeSeconds = Integer.MAX_VALUE;
        }
        Cookie cookie = new Cookie(REFRESH_TOKEN, token);
        cookie.setHttpOnly(true);
        cookie.setSecure(secure);
        cookie.setPath(PATH);
        cookie.setMaxAge((int) maxAgeSeconds);
        cookie.setAttribute("SameSite", "None");
        return cookie;
    }

    public static Cookie deleteRefreshTokenCookie(boolean secure) {
        Cookie cookie = new Cookie(REFRESH_TOKEN, null);
        cookie.setHttpOnly(true);
        cookie.setSecure(secure);
        cookie.setPath(PATH);
        cookie.setMaxAge(0);
        cookie.setAttribute("SameSite", "Lax");
        return cookie;
    }

    public static Cookie deleteRefreshTokenCookie(boolean secure, String path, String sameSite) {
        String safePath = (path == null || path.isBlank()) ? PATH : path;

        String normalizedSameSite =
                (sameSite != null && "Strict".equalsIgnoreCase(sameSite)) ? "Strict" : "Lax";

        Cookie cookie = new Cookie(REFRESH_TOKEN, null);
        cookie.setHttpOnly(true);
        cookie.setSecure(secure);
        cookie.setPath(safePath);
        cookie.setMaxAge(0);
        cookie.setAttribute("SameSite", normalizedSameSite);
        return cookie;
    }

}
