package com.ureca.unity.domain.auth.controller;

import com.ureca.unity.domain.auth.constant.OAuthProvider;
import com.ureca.unity.domain.auth.dto.OAuthLoginResponse;
import com.ureca.unity.domain.auth.dto.OAuthUserInfo;
import com.ureca.unity.domain.auth.service.OAuthService;
import com.ureca.unity.domain.auth.service.oauth.OAuthClient;
import com.ureca.unity.domain.auth.service.oauth.OAuthClientFactory;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
public class OAuthController {

    private final OAuthService oAuthService;
    private final OAuthClientFactory oauthClientFactory;

//    @PostMapping("/login/{provider}")
//    public OAuthLoginResponse login(
//            @PathVariable String provider,
//            @RequestParam @NotBlank String code
//    ) {
//
//        return oAuthService.login(
//                OAuthProvider.from(provider),
//                code
//        );
//    }

    @PostMapping("/login/{provider}")
    public OAuthUserInfo login(
            @PathVariable String provider,
            @RequestParam @NotBlank String code
    ) {
        OAuthProvider oAuthProvider = OAuthProvider.from(provider);
        OAuthClient client = oauthClientFactory.get(oAuthProvider);
        var userInfo = client.getUserInfo(code);
//        return oAuthService.login(oAuthProvider, userInfo);
        return userInfo;
    }

    // refresh / logout은 추후 security 레이어에서 추가
}
