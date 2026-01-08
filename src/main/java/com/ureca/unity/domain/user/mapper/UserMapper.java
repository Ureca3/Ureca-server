package com.ureca.unity.domain.user.mapper;

import com.ureca.unity.domain.user.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {

    User findByProviderAndProviderId(
            @Param("provider") String provider,
            @Param("providerId") String providerId
    );

    void insert(User user);
}
