package com.ureca.unity.domain.auth.mapper;

import com.ureca.unity.domain.auth.model.OAuthToken;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

@Mapper
public interface OAuthTokenMapper {
    void insert(OAuthToken token);
    Optional<OAuthToken> findByUserIdAndProvider(@Param("userId") Long userId, @Param("provider") String provider);
    void deleteByUserId(@Param("userId") Long userId);
    void deleteByUserIdAndProvider(@Param("userId") Long userId, @Param("provider") String provider);
}
