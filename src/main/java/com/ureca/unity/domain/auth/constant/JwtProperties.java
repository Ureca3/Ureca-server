package com.ureca.unity.domain.auth.constant;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        @NotBlank String issuer,
        @NotBlank String secret,
        @Min(1) long accessExpirationSeconds,
        @Min(1) long refreshExpirationSeconds
) {}
