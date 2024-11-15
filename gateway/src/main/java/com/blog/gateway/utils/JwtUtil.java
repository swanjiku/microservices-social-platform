package com.blog.gateway.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;

@Component
public class JwtUtil {

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    public void validateToken(final String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getSignKey()).build().parseClaimsJws(token);
            System.out.println("Token validated successfully in JwtUtil");
        } catch (Exception e) {
            System.out.println("Token validation failed in JwtUtil: " + e.getMessage());
            throw e;
        }
    }

    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
