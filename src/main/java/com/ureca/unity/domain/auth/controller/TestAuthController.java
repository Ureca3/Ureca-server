package com.ureca.unity.domain.auth.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestAuthController {
    @GetMapping("/api/test/auth")
    public String authTest(@AuthenticationPrincipal Long userId) {
        return "인증 성공 userId = " + userId;
    }

}
