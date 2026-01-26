package com.ureca.unity.domain.user.service;

import com.ureca.unity.domain.auth.mapper.RefreshTokenMapper;
import com.ureca.unity.domain.auth.service.OAuthTokenService;
import com.ureca.unity.domain.user.mapper.UserMapper;
import com.ureca.unity.global.exception.CustomException;
import com.ureca.unity.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserWithdrawTxService {

    private final OAuthTokenService oAuthTokenService;
    private final RefreshTokenMapper refreshTokenMapper;
    private final UserMapper userMapper;

    @Transactional
    public void withdrawDbOnly(Long userId) {
        oAuthTokenService.deleteByUserId(userId);
        refreshTokenMapper.deleteByUserId(userId);

        int updated = userMapper.softDeleteById(userId);
        if (updated == 0) throw new CustomException(ErrorCode.USER_ALREADY_DELETED);
    }
}
