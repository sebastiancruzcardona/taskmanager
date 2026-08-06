package com.example.taskmanager.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // Read the authorization header (read the token)
        String authHeader = request.getHeader("Authorization");

        // Validate Bearer token presence: has to be not null and start with Bearer (Bearer Token auth type)
        if(authHeader == null || !authHeader.startsWith("Bearer ")) {
            // Skip the authentication -> Unauthenticated
            filterChain.doFilter(request, response);
            return;
        }

        // Extract the token: remove "Bearer " (0 to 6 characters) string from it
        String token = authHeader.substring(7);

        // Validate JWT
        try {
            // Extract email
            String email = jwtUtil.extractEmail(token);

            // Create Authentication object (The email and the request are valid, user is authenticated)
            UsernamePasswordAuthenticationToken authentication  =
                    new UsernamePasswordAuthenticationToken(
                            email,
                            null,
                            List.of() // The authorization role that te user will have
                    );

            // Attach Authentication object to the security context
            SecurityContextHolder.getContext()
                    .setAuthentication(authentication);

        } catch(Exception e) {
            /* If signature or expiration are not valid this exception is thrown
            and the user is unauthorized */
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // Default behavior
        filterChain.doFilter(request, response);
    }
}
