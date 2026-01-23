package com.ureca.unity.domain.auth.service;

import com.ureca.unity.domain.auth.constant.OAuthProvider;
import com.ureca.unity.domain.auth.dto.OAuthLoginResponse;
import com.ureca.unity.domain.auth.dto.OAuthLoginResult;
import com.ureca.unity.domain.auth.dto.OAuthUserInfo;
import com.ureca.unity.domain.auth.dto.TokenResponse;
import com.ureca.unity.domain.auth.service.oauth.OAuthClient;
import com.ureca.unity.domain.user.mapper.UserMapper;
import com.ureca.unity.domain.user.model.User;
import com.ureca.unity.global.exception.CustomException;
import com.ureca.unity.global.exception.ErrorCode;
import com.ureca.unity.global.security.JwtIssuer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OAuthServiceImpl implements OAuthService {

    private final Map<String, OAuthClient> oauthClients;
    private final UserMapper userMapper;
    private final JwtIssuer jwtIssuer;
    private final RefreshTokenService refreshTokenService;

    @Override
    public OAuthLoginResult login(OAuthProvider provider, String authorizationCode) {
        // 1. OAuthClient 선택
        OAuthClient oAuthClient = oauthClients.get(provider.value());
        if (oAuthClient == null) {
            throw new CustomException(ErrorCode.INVALID_OAUTH_PROVIDER);
        }

        // 2. OAuth 사용자 정보 조회
        OAuthUserInfo userInfo = oAuthClient.getUserInfo(authorizationCode);

        // 3. 사용자 조회
        return userMapper
                .findByProviderAndProviderId(
                        userInfo.getProvider(),
                        userInfo.getProviderId()
                )
                .map(existingUser -> createLoginResult(existingUser.getId(), false)
                )
                .orElseGet(() ->
                        userMapper
                                .findAnyByProviderAndProviderId(userInfo.getProvider(), userInfo.getProviderId())
                                .map(anyUser -> {
                                    // 4-1. 탈퇴 유저면 복구
                                    if (anyUser.getDeletedAt() != null) {
                                        userMapper.restoreById(
                                                anyUser.getId(),
                                                userInfo.getEmail(),
                                                userInfo.getName()
                                        );
                                    }
                                    return createLoginResult(anyUser.getId(), false);
                                })
                .orElseGet(() -> {
                    // 4-2. 신규 사용자 생성
                    User newUser = User.builder()
                            .provider(userInfo.getProvider())
                            .providerId(userInfo.getProviderId())
                            .email(userInfo.getEmail())
                            .name(userInfo.getName())
                            .role("ROLE_USER")
                            .build();
                    userMapper.insert(newUser);
                    return createLoginResult(newUser.getId(), true);
                    })
                );
    }
    private OAuthLoginResult createLoginResult(Long userId, boolean requiresOnboarding) {
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
