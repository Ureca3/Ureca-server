package com.ureca.unity.domain.policy.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

public class PolicyController {
    @PostMapping("/api/policy/agree")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void agree(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        policyService.agree(userId);
    }

}
