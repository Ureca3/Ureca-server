package com.ureca.unity.domain.auth.service;

import com.ureca.unity.domain.auth.mapper.OAuthTokenMapper;
import com.ureca.unity.domain.auth.model.OAuthToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OAuthTokenServiceImpl implements OAuthTokenService {

    private final OAuthTokenMapper oAuthTokenMapper;

    @Transactional
    @Override
    public void upsert(OAuthToken token) {
        oAuthTokenMapper.deleteByUserIdAndProvider(token.getUserId(), token.getProvider());
        oAuthTokenMapper.insert(token);
    }

    @Override
    public Optional<OAuthToken> find(Long userId, String provider) {
        return oAuthTokenMapper.findByUserIdAndProvider(userId, provider);
    }

    @Transactional
    @Override
    public void deleteByUserId(Long userId) {
        oAuthTokenMapper.deleteByUserId(userId);
    }
}
