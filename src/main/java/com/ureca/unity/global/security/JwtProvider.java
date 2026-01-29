package com.ureca.unity.global.security;

import com.ureca.unity.domain.auth.constant.JwtProperties;
import com.ureca.unity.global.exception.CustomException;
import com.ureca.unity.global.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class JwtProvider {

    private final SecretKey key;

    public JwtProvider(JwtProperties props) {
        this.key = Keys.hmacShaKeyFor(
                props.secret().getBytes(StandardCharsets.UTF_8)
        );
    }

    public Long getUserId(String token, String expectedType) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        String tokenType = claims.get("type", String.class);

        if (!expectedType.equals(tokenType)) {
            throw new CustomException(ErrorCode.REFRESH_TOKEN_INVALID);
        }

        return Long.valueOf(claims.getSubject());
    }

    public Authentication getAuthentication(String token) {
        Long userId = getUserId(token, "access");

        return new UsernamePasswordAuthenticationToken(
                userId,
                null,
                List.of()
        );
    }
}
