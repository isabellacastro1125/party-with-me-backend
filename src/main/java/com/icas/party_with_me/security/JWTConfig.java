package com.icas.party_with_me.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class JWTConfig {

    @Value("${jwt.secret}")
    private String jwtSecret;

    public String getJwtSecret() {
        return jwtSecret;
    }
    
    @PostConstruct
    public void logSecret() {
        System.out.println("JWT Secret: " + jwtSecret);
    }
}
