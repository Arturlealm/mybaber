package com.mybarber.compartilhado.documentacao;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class DocumentacaoApiConfig {

    private static final String ESQUEMA_AUTENTICACAO = "tokenAcesso";

    @Bean
    public OpenAPI documentacaoApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("MyBarber API")
                        .version("v1")
                        .description("Faça login em Autenticação, copie o tokenAcesso e use o botão Authorize."))
                .components(new Components().addSecuritySchemes(ESQUEMA_AUTENTICACAO, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(ESQUEMA_AUTENTICACAO));
    }
}
