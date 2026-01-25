package com.ureca.unity.domain.auth.service;

import com.ureca.unity.domain.auth.constant.OAuthProvider;
import com.ureca.unity.domain.auth.dto.*;
import com.ureca.unity.domain.auth.model.OAuthToken;
import com.ureca.unity.domain.auth.service.oauth.OAuthClient;
import com.ureca.unity.domain.user.mapper.UserMapper;
import com.ureca.unity.domain.user.model.User;
import com.ureca.unity.global.exception.CustomException;
import com.ureca.unity.global.exception.ErrorCode;
import com.ureca.unity.global.security.JwtIssuer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OAuthServiceImpl implements OAuthService {

    private final Map<String, OAuthClient> oauthClients;
    private final UserMapper userMapper;
    private final JwtIssuer jwtIssuer;
    private final RefreshTokenService refreshTokenService;
    private final OAuthTokenService oAuthTokenService;

    @Override
    public OAuthLoginResult login(OAuthProvider provider, String authorizationCode) {
        OAuthClient oAuthClient = oauthClients.get(provider.value());
        if (oAuthClient == null) {
            throw new CustomException(ErrorCode.INVALID_OAUTH_PROVIDER);
        }

        OAuthAuthResult auth = oAuthClient.authenticate(authorizationCode);
        OAuthUserInfo userInfo = auth.userInfo();
        OAuthTokenInfo tokenInfo = auth.tokenInfo();

        return userMapper
                .findByProviderAndProviderId(userInfo.getProvider(), userInfo.getProviderId())
                .map(u -> createLoginResult(u.getId(), false, provider.value(), tokenInfo))
                .orElseGet(() ->
                        userMapper.findAnyByProviderAndProviderId(userInfo.getProvider(), userInfo.getProviderId())
                                .map(anyUser -> {
                                    boolean wasDeleted = anyUser.getDeletedAt() != null;

                                    if (wasDeleted) {
                                        userMapper.restoreById(
                                                anyUser.getId(),
                                                userInfo.getEmail(),
                                                userInfo.getName()
                                        );
                                    }

                                    // 재가입이면 true, 기존이면 false
                                    return createLoginResult(anyUser.getId(), wasDeleted, provider.value(), tokenInfo);
                                })
                                .orElseGet(() -> {
                                    User newUser = User.builder()
                                            .provider(userInfo.getProvider())
                                            .providerId(userInfo.getProviderId())
                                            .email(userInfo.getEmail())
                                            .name(userInfo.getName())
                                            .role("ROLE_USER")
                                            .build();
                                    userMapper.insert(newUser);
                                    return createLoginResult(newUser.getId(), true, provider.value(), tokenInfo);
                                })
                );
    }

    private OAuthLoginResult createLoginResult(
            Long userId,
            boolean requiresOnboarding,
            String provider,
            OAuthTokenInfo tokenInfo
    ) {
        oAuthTokenService.upsert(
                OAuthToken.builder()
                        .userId(userId)
                        .provider(provider)
                        .accessToken(tokenInfo.accessToken())
                        .refreshToken(tokenInfo.refreshToken())
                        .expiresAt(
                                tokenInfo.expiresInSeconds() != null
                                        ? LocalDateTime.ofInstant(
                                        Instant.now().plusSeconds(tokenInfo.expiresInSeconds()),
                                        ZoneOffset.UTC
                                )
                                        : null
                        )
                        .build()
        );

        String refreshToken = jwtIssuer.issueRefreshToken(userId);
        refreshTokenService.saveRefreshToken(userId, refreshToken);

        TokenResponse accessToken = jwtIssuer.issueAccessToken(userId);

        return new OAuthLoginResult(
                OAuthLoginResponse.builder()
                        .token(accessToken)
                        .requiresOnboarding(requiresOnboarding)
                        .build(),
                refreshToken
        );
    }
}
