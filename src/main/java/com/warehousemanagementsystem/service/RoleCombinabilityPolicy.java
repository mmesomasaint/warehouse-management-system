package com.warehousemanagementsystem.service;

import com.warehousemanagementsystem.model.security.EmployeeRole;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class RoleCombinabilityPolicy {

    // Pairs of roles that cannot be held simultaneously (Separation of Duties)
    private static final List<Set<EmployeeRole>> MUTUALLY_EXCLUSIVE_PAIRS = List.of(
            Set.of(EmployeeRole.ADMIN, EmployeeRole.CASHIER),
            Set.of(EmployeeRole.HR_MANAGER, EmployeeRole.LOGISTICS_COORDINATOR)
    );

    public void validateCombination(Set<EmployeeRole> roles) {
        if (roles == null || roles.isEmpty()) {
            throw new IllegalArgumentException("At least one role must be assigned to an employee.");
        }

        for (Set<EmployeeRole> forbiddenPair : MUTUALLY_EXCLUSIVE_PAIRS) {
            if (roles.containsAll(forbiddenPair)) {
                throw new IllegalArgumentException(
                        "Conflicting role combination is not allowed: " + forbiddenPair
                );
            }
        }
    }

    public Set<String> resolveAuthorities(Set<EmployeeRole> roles) {
        validateCombination(roles);
        Set<String> resolved = new HashSet<>();
        for (EmployeeRole role : roles) {
            role.getAuthorities().forEach(auth -> resolved.add(auth.getAuthority()));
        }
        return Collections.unmodifiableSet(resolved);
    }
}