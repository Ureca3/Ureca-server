package com.ureca.unity.domain.user.controller;

import com.ureca.unity.domain.user.service.UserService;
import com.ureca.unity.global.util.CookieUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Value("${cookie.secure:false}")
    private boolean cookieSecure;

    @PostMapping("/withdrawal")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void withdrawal(Authentication authentication, HttpServletResponse response) {
        Long userId = (Long) authentication.getPrincipal();

        userService.withdraw(userId);

        response.addCookie(CookieUtils.deleteRefreshTokenCookie(cookieSecure));
    }
}
