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
        this.key = Keys.hmacShaKeyFor(
                props.secret().getBytes(StandardCharsets.UTF_8)
        );
    }

    // 👉 로그인 성공 후 호출되는 유일한 메서드
    public TokenResponse issueTokens(Long userId) {

        long accessExp = props.accessExpirationSeconds();
        long refreshExp = props.refreshExpirationSeconds();

        String accessToken = createToken(userId, accessExp);
        String refreshToken = createToken(userId, refreshExp);

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenExpiresIn(accessExp)
                .refreshTokenExpiresIn(refreshExp)
                .build();
    }

    public TokenResponse issueAccessToken(Long userId) {

        long accessExp = props.accessExpirationSeconds();
        String accessToken = createToken(userId, accessExp);

        return TokenResponse.builder()
                .accessToken(accessToken)
                .accessTokenExpiresIn(accessExp)
                .build();
    }

    private String createToken(Long userId, long expSeconds) {
        Instant now = Instant.now();

        return Jwts.builder()
                .issuer(props.issuer())
                .subject(String.valueOf(userId))
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expSeconds)))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }
}
