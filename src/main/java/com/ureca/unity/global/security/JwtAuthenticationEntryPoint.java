package com.ureca.unity.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ureca.unity.global.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {

        ErrorCode errorCode =
                (ErrorCode) request.getAttribute("authError");

        if (errorCode == null) {
            errorCode = ErrorCode.TOKEN_MISSING;
        }

        response.setStatus(errorCode.getStatus().value());
        response.setContentType("application/json;charset=UTF-8");

        response.getWriter().write(
                objectMapper.writeValueAsString(
                        Map.of(
                                "success", false,
                                "errorCode", errorCode.name(),
                                "message", errorCode.getMessage()
                        )
                )
        );
    }
}
