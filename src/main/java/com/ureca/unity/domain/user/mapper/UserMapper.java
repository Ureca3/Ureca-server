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

    void insert(User user);
}