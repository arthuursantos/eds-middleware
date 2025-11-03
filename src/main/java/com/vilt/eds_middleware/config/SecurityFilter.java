package com.vilt.eds_middleware.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Component
public class SecurityFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(SecurityFilter.class);

    public static final String[] noTokenEndpoints = {
            "/middleware/user/login",
            "/middleware/user/register",
    };

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (!checkEndpoint(request)) {
            String token = recoveryToken(request);
            if (token == null) {
                throw new RuntimeException("Token ausente.");
            }
        }
        filterChain.doFilter(request, response);
        log.info("Response status: {}", response.getStatus());
    }

    private String recoveryToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.replace("Bearer ", "");
        }
        return null;
    }

    private boolean checkEndpoint(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return Arrays.asList(noTokenEndpoints).contains(uri);
    }

}
