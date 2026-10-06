package com.mybarber.autenticacao;

import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Configuration
@EnableMethodSecurity
public class SegurancaConfig {

    public static final String CLAIM_PERFIL = "perfil";
    public static final String CLAIM_NOME = "nome";

    private final HandlerExceptionResolver handlerExceptionResolver;

    public SegurancaConfig(@Qualifier("handlerExceptionResolver") HandlerExceptionResolver handlerExceptionResolver) {
        this.handlerExceptionResolver = handlerExceptionResolver;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        AuthenticationEntryPoint pontoEntradaNaoAutenticado = (requisicao, resposta, excecao) ->
                handlerExceptionResolver.resolveException(requisicao, resposta, null, excecao);
        AccessDeniedHandler tratadorAcessoNegado = (requisicao, resposta, excecao) ->
                handlerExceptionResolver.resolveException(requisicao, resposta, null, excecao);

        return http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(sessao -> sessao.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(autorizacao -> autorizacao
                        .requestMatchers(HttpMethod.POST, "/api/clientes", "/api/autenticacao/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/servicos", "/api/servicos/combinacoes").permitAll()
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(servidorRecursos -> servidorRecursos
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(conversorAutenticacaoJwt()))
                        .authenticationEntryPoint(pontoEntradaNaoAutenticado)
                        .accessDeniedHandler(tratadorAcessoNegado))
                .exceptionHandling(excecoes -> excecoes
                        .authenticationEntryPoint(pontoEntradaNaoAutenticado)
                        .accessDeniedHandler(tratadorAcessoNegado))
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtEncoder jwtEncoder(JwtPropriedades jwtPropriedades) {
        return NimbusJwtEncoder.withSecretKey(chaveAssinatura(jwtPropriedades)).build();
    }

    @Bean
    public JwtDecoder jwtDecoder(JwtPropriedades jwtPropriedades) {
        NimbusJwtDecoder decodificador = NimbusJwtDecoder.withSecretKey(chaveAssinatura(jwtPropriedades))
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        decodificador.setJwtValidator(JwtValidators.createDefaultWithIssuer(jwtPropriedades.emissor()));
        return decodificador;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(CorsPropriedades corsPropriedades) {
        CorsConfiguration configuracao = new CorsConfiguration();
        configuracao.setAllowedOrigins(corsPropriedades.origensPermitidas());
        configuracao.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuracao.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource fonte = new UrlBasedCorsConfigurationSource();
        fonte.registerCorsConfiguration("/api/**", configuracao);
        return fonte;
    }

    private JwtAuthenticationConverter conversorAutenticacaoJwt() {
        JwtGrantedAuthoritiesConverter conversorPerfis = new JwtGrantedAuthoritiesConverter();
        conversorPerfis.setAuthoritiesClaimName(CLAIM_PERFIL);
        conversorPerfis.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter conversor = new JwtAuthenticationConverter();
        conversor.setJwtGrantedAuthoritiesConverter(conversorPerfis);
        return conversor;
    }

    private SecretKey chaveAssinatura(JwtPropriedades jwtPropriedades) {
        return new SecretKeySpec(jwtPropriedades.segredo().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }
}
