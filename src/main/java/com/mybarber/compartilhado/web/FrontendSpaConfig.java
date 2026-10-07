package com.mybarber.compartilhado.web;

import java.io.IOException;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

/* Entrega o frontend React empacotado em /static; rotas da aplicação (ex.: /agendar) devolvem o index.html */
@Configuration
public class FrontendSpaConfig implements WebMvcConfigurer {

    private static final String PASTA_FRONTEND = "classpath:/static/";

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registro) {
        registro.addResourceHandler("/**")
                .addResourceLocations(PASTA_FRONTEND)
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String caminho, Resource local) throws IOException {
                        Resource solicitado = local.createRelative(caminho);
                        if (solicitado.exists() && solicitado.isReadable()) {
                            return solicitado;
                        }
                        if (caminho.startsWith("api/") || caminho.contains(".")) {
                            return null;
                        }
                        Resource paginaInicial = new ClassPathResource("static/index.html");
                        return paginaInicial.exists() ? paginaInicial : null;
                    }
                });
    }
}
