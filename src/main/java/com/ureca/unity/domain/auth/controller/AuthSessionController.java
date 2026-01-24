package com.ureca.unity.domain.auth.controller;

import com.ureca.unity.domain.auth.dto.MeResponse;
import com.ureca.unity.domain.auth.service.AuthSessionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
