package com.ureca.unity.domain.user.service;

import com.ureca.unity.domain.auth.mapper.RefreshTokenMapper;
import com.ureca.unity.domain.user.mapper.UserMapper;
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

    @Transactional
    @Override
    public void withdraw(Long userId) {
        // 1. 사용자 존재/상태 확인
        userMapper.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 2. RefreshToken(DB) 삭제 (전체 로그아웃 효과)
        refreshTokenMapper.deleteByUserId(userId);

        // 3. users.soft delete
        int updated = userMapper.softDeleteById(userId);
        if (updated == 0) {
            // 이미 deleted_at 찍힌 경우
            throw new CustomException(ErrorCode.USER_ALREADY_DELETED);
        }
    }
}
