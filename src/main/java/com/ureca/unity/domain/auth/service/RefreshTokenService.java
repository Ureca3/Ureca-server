package com.ureca.unity.domain.auth.service;

import com.ureca.unity.domain.auth.dto.TokenResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface RefreshTokenService {
    TokenResponse refreshAccessToken(HttpServletRequest request, HttpServletResponse response);
}
