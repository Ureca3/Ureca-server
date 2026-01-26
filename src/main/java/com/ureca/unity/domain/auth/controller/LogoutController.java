package com.ureca.unity.domain.auth.controller;

import com.ureca.unity.domain.auth.service.LogoutService;
import com.ureca.unity.global.util.CookieUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "1. Auth",
        description = "회원가입 및 로그인 관련 API"
)
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class LogoutController {

    private final LogoutService logoutService;

    @Value("${cookie.secure:false}")
    private boolean cookieSecure;

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.OK)
    public void logout(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response
    ) {
        logoutService.logout(refreshToken);
        response.addCookie(
                CookieUtils.deleteRefreshTokenCookie(cookieSecure)
        );
    }
}