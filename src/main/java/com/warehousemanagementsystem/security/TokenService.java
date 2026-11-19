package com.warehousemanagementsystem.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;
import java.util.List;

@Service
public class TokenService {

    private final JwtEncoder encoder;
    private final RsaKeyProperties rsaKeys;
    private final long expirationMinutes;

    public TokenService(JwtEncoder encoder, RsaKeyProperties rsaKeys) {
        this.encoder = encoder;
        this.rsaKeys = rsaKeys;
        this.expirationMinutes = 60L;
    }

    public String generateToken(Authentication authentication) {
        EmployeePrincipal userPrincipal = (EmployeePrincipal) authentication.getPrincipal();
        Instant now = Instant.now();

        List<String> authorities = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList());

        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer("warehouse-management-system")
            .issuedAt(now)
            .expiresAt(now.plus(rsaKeys.expirationMinutes(), ChronoUnit.MINUTES))
            .subject(userPrincipal.getUsername()) // Email
            .claim("employeeId", userPrincipal.getEmployeeId())
            .claim("storeId", userPrincipal.getStoreId())
            .claim("authorities", authorities)
            .build();

        return this.encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}
