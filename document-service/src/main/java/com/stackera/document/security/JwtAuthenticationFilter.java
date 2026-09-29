package com.stackera.document.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        System.out.println("========== JWT FILTER ==========");
        System.out.println("Request: " + request.getMethod() + " " + request.getRequestURI());
        System.out.println("Authorization header present: " + (authHeader != null));

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {

            System.out.println("JWT: No Bearer token found");

            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7).trim();

        System.out.println("JWT token received: YES");

        try {

            boolean valid = jwtService.isTokenValid(token);

            System.out.println("JWT validation result: " + valid);

            if (valid) {

                String email = jwtService.extractUsername(token);

                System.out.println("JWT username/email: " + email);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                email,
                                null,
                                Collections.emptyList()
                        );

                authentication.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request)
                );

                SecurityContextHolder
                        .getContext()
                        .setAuthentication(authentication);

                System.out.println("JWT authentication SUCCESS");
            } else {

                System.out.println("JWT authentication FAILED: token is invalid");

                SecurityContextHolder.clearContext();
            }

        } catch (Exception e) {

            System.out.println("JWT validation EXCEPTION: "
                    + e.getClass().getSimpleName());

            System.out.println("JWT validation message: "
                    + e.getMessage());

            SecurityContextHolder.clearContext();
        }

        System.out.println("Authenticated user: "
                + SecurityContextHolder.getContext().getAuthentication());

        System.out.println("================================");

        filterChain.doFilter(request, response);
    }
}