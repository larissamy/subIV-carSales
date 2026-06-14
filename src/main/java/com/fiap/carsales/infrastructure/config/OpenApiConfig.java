package com.fiap.carsales.infrastructure.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Car Sales Main API",
                version = "v1",
                description = "Software principal para cadastro, edição e controle de status de veículos"
        )
)
public class OpenApiConfig {
}
