package com.ureca.unity.domain.auth.service;

import com.ureca.unity.domain.auth.constant.JwtProperties;
import com.ureca.unity.domain.auth.constant.OAuthProvider;
import com.ureca.unity.domain.auth.dto.OAuthLoginResponse;
import com.ureca.unity.domain.auth.dto.OAuthUserInfo;
import com.ureca.unity.domain.auth.dto.TokenResponse;
import com.ureca.unity.domain.auth.mapper.RefreshTokenMapper;
import com.ureca.unity.domain.auth.model.RefreshToken;
import com.ureca.unity.domain.auth.service.oauth.OAuthClient;
import com.ureca.unity.domain.user.mapper.UserMapper;
import com.ureca.unity.domain.user.model.User;
import com.ureca.unity.global.security.JwtIssuer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OAuthServiceImpl implements OAuthService {

    private final Map<String, OAuthClient> oauthClients;
    private final UserMapper userMapper;
    private final JwtIssuer jwtIssuer;

    private final RefreshTokenMapper refreshTokenMapper;
    private final JwtProperties jwtProperties;

    @Override
    public OAuthLoginResponse login(OAuthProvider provider, String authorizationCode) {
        // 1. OAuthClient 선택
        OAuthClient oAuthClient = oauthClients.get(provider.value());
        if (oAuthClient == null) {
            throw new IllegalArgumentException("OAuthClient not found for provider: " + provider);
        }

        // 2. OAuth 사용자 정보 조회
        OAuthUserInfo userInfo = oAuthClient.getUserInfo(authorizationCode);

        // 3. 사용자 조회
        return userMapper
                .findByProviderAndProviderId(
                        userInfo.getProvider(),
                        userInfo.getProviderId()
                )
                .map(existingUser -> {

                    TokenResponse token = jwtIssuer.issueTokens(existingUser.getId());

                    saveRefreshToken(existingUser.getId(), token.getRefreshToken());

                    return OAuthLoginResponse.builder()
                            .token(token)                // JWT 아직 없음
                            .requiresOnboarding(false)  // 기존 유저
                            .build();
                })
                .orElseGet(() -> {
                    // 4. 신규 사용자 생성
                    User newUser = User.builder()
                            .provider(userInfo.getProvider())
                            .providerId(userInfo.getProviderId())
                            .email(userInfo.getEmail())
                            .name(userInfo.getName())
                            .role("ROLE_USER")
                            .build();

                    userMapper.insert(newUser);

                    TokenResponse token = jwtIssuer.issueTokens(newUser.getId());

                    saveRefreshToken(newUser.getId(), token.getRefreshToken());

                    return OAuthLoginResponse.builder()
                            .token(token)
                            .requiresOnboarding(true)   // 신규 유저
                            .build();
                });
    }

    private void saveRefreshToken(Long userId, String refreshToken) {
        refreshTokenMapper.deleteByUserId(userId); // 기존 토큰 정리 (선택 but 추천)

        refreshTokenMapper.insert(
                RefreshToken.builder()
                        .userId(userId)
                        .token(refreshToken)
                        .expiresAt(
                                LocalDateTime.now()
                                        .plusSeconds(jwtProperties.refreshExpirationSeconds())
                        )
                        .build()
        );
    }
}
