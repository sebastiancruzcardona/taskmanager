package com.example.taskmanager.security;

import com.example.taskmanager.enums.RoleEnum;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKey;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    public String generateToken(String email, RoleEnum role) {
        return Jwts.builder()
                .subject(email) // who creates it (this goes in the payload)
                .claim("role", role) // key, value (this goes in the payload)
                .issuedAt(new Date())
                .expiration(
                        new Date(System.currentTimeMillis() + 60 * 60 * 1000) // 1h
                )
                .signWith(getSigningKey())
                .compact();
    }

    /* Validate the token:
     - verify the signature
     - check expiration date
     - ensure token integrity */
    public String extractEmail(String token) {
        // Reverse the token data
        return Jwts.parser()
                .setSigningKey(getSigningKey()) // The secret key
                .build()
                .parseSignedClaims(token)// Validate signature and the expiration (or throw exception)
                .getPayload() // The data inside our token. // .get("role") to bring the role
                .getSubject(); // Our goal is to extract the email
    }

    public RoleEnum extractRole(String token) {
        return RoleEnum.valueOf(
                Jwts.parser()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .get("role")
                    .toString()
        );
    }
}
