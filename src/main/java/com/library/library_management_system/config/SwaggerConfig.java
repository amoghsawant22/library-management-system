package com.library.library_management_system.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger/OpenAPI 3 configuration for API documentation
 */
@Configuration
@RequiredArgsConstructor
public class SwaggerConfig {

    @Value("${server.port:8080}")
    private int serverPort;

    @Value("${spring.application.name:library-management-system}")
    private String applicationName;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(serverList())
                .addSecurityItem(securityRequirement())
                .components(securityComponents());
    }

    private Info apiInfo() {
        return new Info()
                .title("Nalanda Library Management System API")
                .description("""
                    A comprehensive library management system built with Spring Boot.
                    
                    **Features:**
                    - User Management (Admin/Member roles)
                    - Book Management with advanced search
                    - Borrowing System with overdue tracking
                    - Fine Management
                    - Comprehensive Reporting
                    - JWT-based Authentication
                    - GraphQL API support
                    
                    **Authentication:**
                    1. Register or login to get JWT token
                    2. Include token in Authorization header: `Bearer <token>`
                    3. Tokens expire in 24 hours, refresh tokens in 7 days
                    
                    **User Roles:**
                    - **ADMIN**: Full access to all operations
                    - **MEMBER**: Can borrow/return books, view own history
                    """)
                .version("1.0.0")
                .contact(contact())
                .license(license());
    }

    private Contact contact() {
        return new Contact()
                .name("Library Management Team")
                .email("admin@library.com")
                .url("https://library.com");
    }

    private License license() {
        return new License()
                .name("MIT License")
                .url("https://opensource.org/licenses/MIT");
    }

    private List<Server> serverList() {
        return List.of(
                new Server()
                        .url("http://localhost:" + serverPort)
                        .description("Development Server"),
                new Server()
                        .url("https://api.library.com")
                        .description("Production Server")
        );
    }

    private SecurityRequirement securityRequirement() {
        return new SecurityRequirement().addList("Bearer Authentication");
    }

    private Components securityComponents() {
        return new Components()
                .addSecuritySchemes("Bearer Authentication", securityScheme());
    }

    private SecurityScheme securityScheme() {
        return new SecurityScheme()
                .name("Bearer Authentication")
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .description("JWT Authorization header using the Bearer scheme. Example: \"Authorization: Bearer {token}\"");
    }
}