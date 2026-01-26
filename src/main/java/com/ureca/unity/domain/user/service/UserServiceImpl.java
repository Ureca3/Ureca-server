package com.ureca.unity.domain.user.service;

import com.ureca.unity.domain.auth.mapper.RefreshTokenMapper;
import com.ureca.unity.domain.auth.model.OAuthToken;
import com.ureca.unity.domain.auth.service.OAuthTokenService;
import com.ureca.unity.domain.user.mapper.UserMapper;
import com.ureca.unity.domain.user.model.User;
import com.ureca.unity.domain.user.service.unlink.SocialUnlinkService;
import com.ureca.unity.global.exception.CustomException;
import com.ureca.unity.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final OAuthTokenService oAuthTokenService;
    private final SocialUnlinkService socialUnlinkService;
    private final UserWithdrawTxService userWithdrawTxService;

    @Override
    public void withdraw(Long userId) {
        User user = userMapper.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        String provider = user.getProvider();
        if (provider != null && !provider.isBlank()) {
            OAuthToken token = oAuthTokenService.find(userId, provider)
                    .orElseThrow(() -> new CustomException(ErrorCode.OAUTH_TOKEN_NOT_FOUND));

            // 1. 트랜잭션 밖에서 unlink 먼저 (실패하면 탈퇴 중단)
            socialUnlinkService.unlink(user, token);
        }

        // 2. DB 정리는 트랜잭션으로
        userWithdrawTxService.withdrawDbOnly(userId);
    }
}
