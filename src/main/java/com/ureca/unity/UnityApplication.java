package com.ureca.unity;

import com.ureca.unity.domain.auth.constant.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class UnityApplication {

    public static void main(String[] args) {
        SpringApplication.run(UnityApplication.class, args);
    }

}
