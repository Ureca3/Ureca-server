package com.ureca.unity.domain.auth.service;

import com.ureca.unity.domain.auth.constant.OAuthProvider;
import com.ureca.unity.domain.auth.dto.OAuthLoginResponse;
import com.ureca.unity.domain.auth.dto.OAuthUserInfo;
import com.ureca.unity.domain.auth.service.oauth.OAuthClient;
import com.ureca.unity.domain.user.mapper.UserMapper;
import com.ureca.unity.domain.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class OAuthServiceImpl implements OAuthService {

    private final Map<String, OAuthClient> oauthClients;
    private final UserMapper userMapper;

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
                .map(existingUser ->
                        OAuthLoginResponse.builder()
                                .token(null)                // JWT 아직 없음
                                .requiresOnboarding(false)  // 기존 유저
                                .build()
                )
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

                    return OAuthLoginResponse.builder()
                            .token(null)
                            .requiresOnboarding(true)   // 신규 유저
                            .build();
                });
    }
}
