package com.stackera.document.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;


@Configuration
public class SecurityConfig {


    // ============================================================
    // JWT FILTER
    // ============================================================

    private final JwtAuthenticationFilter jwtFilter;


    public SecurityConfig(JwtAuthenticationFilter jwtFilter) {

        this.jwtFilter = jwtFilter;

    }


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
                        cors.configurationSource(
                                corsConfigurationSource()
                        )
                )


                // ------------------------------------------------
                // Stateless JWT authentication
                // ------------------------------------------------

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )


                // ------------------------------------------------
                // Authorization
                // ------------------------------------------------

                .authorizeHttpRequests(auth -> auth

                        // Browser CORS preflight request
                        .requestMatchers(
                                org.springframework.http.HttpMethod.OPTIONS,
                                "/**"
                        )
                        .permitAll()

                        // Public test endpoint
                        .requestMatchers(
                                "/api/v1/documents/test"
                        )
                        .permitAll()

                        // Everything else requires JWT
                        .anyRequest()
                        .authenticated()
                )


                // ------------------------------------------------
                // JWT filter
                // ------------------------------------------------

                .addFilterBefore(
                        jwtFilter,
                        UsernamePasswordAuthenticationFilter.class
                );


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
        // Frontend origins
        // --------------------------------------------------------

        configuration.setAllowedOrigins(
                List.of(
                        "http://localhost:63342",
                        "http://127.0.0.1:63342"
                )
        );


        // --------------------------------------------------------
        // Allowed HTTP methods
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
        // Allowed request headers
        // --------------------------------------------------------

        configuration.setAllowedHeaders(
                List.of(
                        "Authorization",
                        "Content-Type",
                        "Accept",
                        "Origin",
                        "X-Requested-With"
                )
        );


        // --------------------------------------------------------
        // Exposed response headers
        // --------------------------------------------------------

        configuration.setExposedHeaders(
                List.of(
                        "Authorization"
                )
        );


        // --------------------------------------------------------
        // JWT is stored in localStorage.
        // We are NOT using cookies.
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

}