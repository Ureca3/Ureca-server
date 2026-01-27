package com.ureca.unity.global.security;

import com.ureca.unity.domain.auth.constant.JwtProperties;
import com.ureca.unity.domain.auth.dto.TokenResponse;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtIssuer {

    private final JwtProperties props;
    private final SecretKey key;

    public JwtIssuer(JwtProperties props) {
        this.props = props;
        byte[] secretBytes = props.secret().getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < 32) {
            throw new IllegalArgumentException(
                    "JWT secret must be at least 256 bits (32 bytes) for HS256"
            );
        }

        this.key = Keys.hmacShaKeyFor(secretBytes);
    }

    public TokenResponse issueAccessToken(Long userId) {

        long accessExp = props.accessExpirationSeconds();
        String accessToken = createToken(userId, accessExp, "access");

        return TokenResponse.builder()
                .accessToken(accessToken)
                .userId(userId)
                .accessTokenExpiresIn(accessExp)
                .build();
    }

    public String issueRefreshToken(Long userId) {
        return createToken(userId, props.refreshExpirationSeconds(), "refresh");
    }

    private String createToken(Long userId, long expSeconds, String type) {
        if (userId == null) {
            throw new IllegalArgumentException("userId must not be null");
        }

        Instant now = Instant.now();

        return Jwts.builder()
                .issuer(props.issuer())
                .subject(String.valueOf(userId))
                .claim("type", type)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expSeconds)))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }
}
