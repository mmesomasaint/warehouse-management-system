package com.warehousemanagementsystem.security;

import io.jsonwebtoken.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtTokenProvider {

    private final RsaKeyProperties rsaKeyProperties;

    public JwtTokenProvider(RsaKeyProperties rsaKeyProperties) {
        this.rsaKeyProperties = rsaKeyProperties;
    }

    public String generateToken(Authentication authentication) {
        EmployeePrincipal userPrincipal = (EmployeePrincipal) authentication.getPrincipal();

        Date now = new Date();
        // Convert expiration minutes from record to milliseconds
        long expirationMillis = rsaKeyProperties.expirationMinutes() * 60 * 1000;
        Date expiryDate = new Date(now.getTime() + expirationMillis);

        return Jwts.builder()
                .setSubject(userPrincipal.getUsername()) // Email
                .claim("employeeId", userPrincipal.getEmployeeId())
                .claim("storeId", userPrincipal.getStoreId())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(rsaKeyProperties.privateKey(), SignatureAlgorithm.RS256) // Uses RSA private key record method
                .compact();
    }

    public String getUsernameFromJWT(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(rsaKeyProperties.publicKey()) // Uses RSA public key record method
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(rsaKeyProperties.publicKey()) // Uses RSA public key record method
                    .build()
                    .parseClaimsJws(authToken);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // Log token validation error (expired, malformed, signature mismatch, etc.)
        }
        return false;
    }
}