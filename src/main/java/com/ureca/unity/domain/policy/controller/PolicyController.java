package com.ureca.unity.domain.policy.controller;

import com.ureca.unity.domain.policy.service.PolicyService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "2. Policy",
        description = "약관 동의 관련 API"
)
@RestController
@RequestMapping("/api/policy")
@RequiredArgsConstructor
public class PolicyController {

    private final PolicyService policyService;

    @PostMapping("/agree")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void agree(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        policyService.agree(userId);
    }
}
