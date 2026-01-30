package com.ureca.unity.domain.policy.service;

import com.ureca.unity.domain.user.mapper.UserMapper;
import com.ureca.unity.domain.user.model.User;
import com.ureca.unity.global.exception.CustomException;
import com.ureca.unity.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class PolicyServiceImpl implements PolicyService {

    private final UserMapper userMapper;

    @Override
    @Transactional
    public void agree(Long userId) {
        User user = userMapper.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (user.getTermsAgreedAt() != null) {
            return;
        }

        userMapper.updateTermsAgreedAt(userId, Instant.now());
    }
}
