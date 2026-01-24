package com.ureca.unity.domain.user.service;

import com.ureca.unity.domain.auth.mapper.RefreshTokenMapper;
import com.ureca.unity.domain.user.mapper.UserMapper;
import com.ureca.unity.domain.user.model.User;
import com.ureca.unity.global.exception.CustomException;
import com.ureca.unity.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final RefreshTokenMapper refreshTokenMapper;

    private final com.ureca.unity.domain.auth.service.OAuthTokenService oAuthTokenService;
    private final com.ureca.unity.domain.user.service.unlink.SocialUnlinkService socialUnlinkService;

    @Transactional
    @Override
    public void withdraw(Long userId) {
        User user = userMapper.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 1. provider token 조회
        com.ureca.unity.domain.auth.model.OAuthToken token =
                oAuthTokenService.find(userId, user.getProvider())
                        .orElseThrow(() -> new CustomException(ErrorCode.OAUTH_TOKEN_NOT_FOUND));

        // 2. 소셜 unlink/revoke
        socialUnlinkService.unlink(user, token);

        // 3. 토큰 정리 (Unity 서비스 쪽)
        oAuthTokenService.deleteByUserId(userId);
        refreshTokenMapper.deleteByUserId(userId);

        // 3. soft delete
        int updated = userMapper.softDeleteById(userId);
        if (updated == 0) {
            throw new CustomException(ErrorCode.USER_ALREADY_DELETED);
        }
    }
}
