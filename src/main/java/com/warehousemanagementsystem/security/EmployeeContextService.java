package com.warehousemanagementsystem.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class EmployeeContextService {
    public Long getCurrentEmployeeId() {
        Jwt jwt = getCurrentJwt();
        return jwt.getClaim("employeeId");
    }

    public UUID getCurrentStoreId() {
        Jwt jwt = getCurrentJwt();
        String storeIdStr = jwt.getClaimAsString("storeId");
        return storeIdStr != null ? UUID.fromString(storeIdStr) : null;
    }

    public String getCurrentUserEmail() {
        Jwt jwt = getCurrentJwt();
        return jwt.getSubject();
    }

    public Jwt getCurrentJwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found in security context.");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof Jwt) {
            return (Jwt) principal;
        }

        throw new IllegalStateException("Expected principal of type Jwt but found: " +
            (principal != null ? principal.getClass().getName() : "null")
        );
    }
}