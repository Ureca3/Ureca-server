package com.ureca.unity.domain.auth.controller;

import com.ureca.unity.domain.auth.constant.JwtProperties;
import com.ureca.unity.domain.auth.constant.OAuthProvider;
import com.ureca.unity.domain.auth.dto.OAuthLoginResponse;
import com.ureca.unity.domain.auth.dto.OAuthLoginResult;
import com.ureca.unity.domain.auth.service.OAuthService;
import com.ureca.unity.global.util.CookieUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "1. Auth",
        description = "회원가입 및 로그인 관련 API"
)
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
public class OAuthController {

    private final OAuthService oAuthService;
    private final JwtProperties jwtProperties;

    @Value("${cookie.secure:false}")
    private boolean cookieSecure;

    @PostMapping("/login/{provider}")
    public OAuthLoginResponse login(
            @PathVariable String provider,
            @RequestParam @NotBlank String code,
            HttpServletResponse response
    ) {
        OAuthLoginResult result = oAuthService.login(OAuthProvider.from(provider), code);

        // 1. 레거시(/api/auth) 쿠키 제거 (예전 SameSite가 Strict였다면 Strict로 한 번 더)
        response.addCookie(CookieUtils.deleteRefreshTokenCookie(cookieSecure, "/api/auth", "Strict"));
        response.addCookie(CookieUtils.deleteRefreshTokenCookie(cookieSecure, "/api/auth", "Lax"));

        // 2. 정상(/) 쿠키 설정
        response.addCookie(
            CookieUtils.createRefreshTokenCookie(
                    result.refreshToken(),
                    jwtProperties.refreshExpirationSeconds(),
                    cookieSecure
            )
        );
        return result.response();
    }
}
