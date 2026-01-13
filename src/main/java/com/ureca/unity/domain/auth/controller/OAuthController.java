package com.ureca.unity.domain.auth.controller;

import com.ureca.unity.domain.auth.constant.JwtProperties;
import com.ureca.unity.domain.auth.constant.OAuthProvider;
import com.ureca.unity.domain.auth.dto.OAuthLoginResponse;
import com.ureca.unity.domain.auth.dto.OAuthLoginResult;
import com.ureca.unity.domain.auth.service.OAuthService;
import com.ureca.unity.global.util.CookieUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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

        response.addCookie(
            CookieUtils.createRefreshTokenCookie(
                    result.refreshToken(),
                    jwtProperties.refreshExpirationSeconds(),
                    cookieSecure
            )
        );
        return result.response();
    }

    // refresh / logout은 추후 security 레이어에서 추가
}
