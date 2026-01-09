package com.ureca.unity.domain.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class KakaoUserResponse {

    private Long id;

    @JsonProperty(value = "kakao_account", required = true)
    private KakaoAccount kakaoAccount;

    @Getter
    public static class KakaoAccount {
        private String email;
        private Profile profile;
    }

    @Getter
    public static class Profile {
        private String nickname;
    }
}
