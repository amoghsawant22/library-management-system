package com.library.library_management_system.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * GraphQL Configuration - Simple working version
 */
@Configuration
@Slf4j
public class GraphQLConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSourceGraphQL() {
        log.info("Configuring CORS for GraphQL endpoints");

        CorsConfiguration configuration = new CorsConfiguration();

        // Allow specific origins for GraphQL
        configuration.setAllowedOriginPatterns(List.of(
                "http://localhost:3000",
                "http://localhost:4200",
                "http://localhost:8080",
                "https://*.yourdomain.com"
        ));

        configuration.setAllowedMethods(List.of("GET", "POST", "OPTIONS"));
        configuration.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "Accept",
                "X-Requested-With"
        ));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/graphql/**", configuration);
        return source;
    }
}