package com.ureca.unity.domain.user.mapper;

import com.ureca.unity.domain.user.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.Optional;

@Mapper
public interface UserMapper {

    Optional<User> findByProviderAndProviderId(
            @Param("provider") String provider,
            @Param("providerId") String providerId
    );

    Optional<User> findAnyByProviderAndProviderId(
            @Param("provider") String provider,
            @Param("providerId") String providerId
    );

    Optional<User> findById(@Param("userId") Long userId);

    void insert(User user);

    // 탈퇴 처리 (deleted_at 찍기)
    int softDeleteById(@Param("userId") Long userId);

    int restoreById(
            @Param("userId") Long userId,
            @Param("email") String email,
            @Param("name") String name
    );
}