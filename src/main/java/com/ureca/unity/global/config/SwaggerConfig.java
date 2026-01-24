package com.ureca.unity.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .tags(List.of(
                        new io.swagger.v3.oas.models.tags.Tag().name("1. Auth"),
                        new io.swagger.v3.oas.models.tags.Tag().name("2. Exam"),
                        new io.swagger.v3.oas.models.tags.Tag().name("3. STT"),
                        new io.swagger.v3.oas.models.tags.Tag().name("4. Summary")
                ))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        )
                )
                .addSecurityItem(
                        new SecurityRequirement().addList("bearerAuth")
                );
    }
}
