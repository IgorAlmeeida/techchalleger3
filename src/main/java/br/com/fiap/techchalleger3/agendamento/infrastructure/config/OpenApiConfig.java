package br.com.fiap.techchalleger3.agendamento.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_AUTH = "bearerAuth";

    static {
        SpringDocUtils.getConfig()
                .replaceWithSchema(LocalDate.class, new StringSchema().example("2026-01-15"))
                .replaceWithSchema(LocalTime.class, new StringSchema().example("09:00:00"))
                .replaceWithSchema(LocalDateTime.class, new StringSchema().example("2026-01-15 10:30:00"));
    }

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Sistema de Agendamento Beleza e Bem-Estar")
                        .description("API REST para agendamento de serviços de beleza e bem-estar. " +
                                "Autenticação via JWT Bearer Token. " +
                                "Roles disponíveis: ADMIN, PROFISSIONAL, CLIENTE.")
                        .version("1.0.0"))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                                .name(BEARER_AUTH)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
