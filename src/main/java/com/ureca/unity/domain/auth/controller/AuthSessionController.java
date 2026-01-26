package com.ureca.unity.domain.auth.controller;

import com.ureca.unity.domain.auth.dto.MeResponse;
import com.ureca.unity.domain.auth.service.AuthSessionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(
        name = "1. Auth",
        description = "회원가입 및 로그인 관련 API"
)
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthSessionController {

    private final AuthSessionService authSessionService;

    @GetMapping("/me")
    public MeResponse me(HttpServletRequest request) {
        return authSessionService.getMe(request);
    }
}
