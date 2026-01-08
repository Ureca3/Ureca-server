package com.ureca.unity.domain.auth.controller;

import com.ureca.unity.domain.auth.constant.OAuthProvider;
import com.ureca.unity.domain.auth.dto.OAuthLoginResponse;
import com.ureca.unity.domain.auth.service.OAuthService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
public class OAuthController {

    private final OAuthService oAuthService;

    @PostMapping("/login/{provider}")
    public OAuthLoginResponse login(
            @PathVariable String provider,
            @RequestParam @NotBlank String code
    ) {
        return oAuthService.login(
                OAuthProvider.from(provider),
                code
        );
    }

    // refresh / logout은 추후 security 레이어에서 추가
}
