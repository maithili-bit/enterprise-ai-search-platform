package com.stackera.auth.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    // ============================================================
    // SECURITY FILTER CHAIN
    // ============================================================

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http

                // ------------------------------------------------
                // Disable CSRF
                // ------------------------------------------------

                .csrf(csrf -> csrf.disable())

                // ------------------------------------------------
                // Enable CORS
                // ------------------------------------------------

                .cors(cors ->
                        cors.configurationSource(corsConfigurationSource())
                )

                // ------------------------------------------------
                // Authorization
                // ------------------------------------------------

                .authorizeHttpRequests(auth -> auth

                        // CORS preflight request
                        .requestMatchers("OPTIONS", "/**")
                        .permitAll()

                        // Login / Register APIs
                        .requestMatchers(
                                "/api/v1/auth/**"
                        )
                        .permitAll()

                        // Everything else requires authentication
                        .anyRequest()
                        .authenticated()
                )

                // ------------------------------------------------
                // HTTP Basic
                // ------------------------------------------------

                .httpBasic(Customizer.withDefaults());

        return http.build();
    }


    // ============================================================
    // CORS CONFIGURATION
    // ============================================================

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration =
                new CorsConfiguration();

        // --------------------------------------------------------
        // FRONTEND ORIGINS
        // --------------------------------------------------------

        configuration.setAllowedOriginPatterns(
                List.of(
                        "http://localhost:63342",
                        "http://127.0.0.1:63342"
                )
        );


        // --------------------------------------------------------
        // ALLOWED METHODS
        // --------------------------------------------------------

        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "PATCH",
                        "DELETE",
                        "OPTIONS"
                )
        );


        // --------------------------------------------------------
        // ALLOWED HEADERS
        // --------------------------------------------------------

        configuration.setAllowedHeaders(
                List.of(
                        "*"
                )
        );


        // --------------------------------------------------------
        // EXPOSED HEADERS
        // --------------------------------------------------------

        configuration.setExposedHeaders(
                List.of(
                        "Authorization"
                )
        );


        // --------------------------------------------------------
        // Credentials
        // --------------------------------------------------------

        configuration.setAllowCredentials(false);


        // --------------------------------------------------------
        // Register CORS configuration
        // --------------------------------------------------------

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration
        );

        return source;
    }


    // ============================================================
    // PASSWORD ENCODER
    // ============================================================

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }
}