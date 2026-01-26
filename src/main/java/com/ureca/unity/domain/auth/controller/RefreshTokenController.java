package com.ureca.unity.domain.auth.controller;

import com.ureca.unity.domain.auth.dto.TokenResponse;
import com.ureca.unity.domain.auth.service.RefreshTokenService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "1. Auth",
        description = "회원가입 및 로그인 관련 API"
)
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class RefreshTokenController {

    private final RefreshTokenService refreshTokenService;

    @PostMapping("/refresh")
    public TokenResponse refresh(HttpServletRequest request,
                                 HttpServletResponse response) {
        return refreshTokenService.refreshAccessToken(request, response);
    }
}
