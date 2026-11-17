package com.warehousemanagementsystem.security;

import com.warehousemanagementsystem.model.Employee;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.UUID;

public class EmployeePrincipal implements UserDetails {

    private final Employee employee;

    public EmployeePrincipal(Employee employee) {
        this.employee = employee;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Automatically pulls roles and granular permissions from the enum!
        return employee.getRole().getAuthorities();
    }

    @Override
    public String getPassword() {
        return employee.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return employee.getEmail();
    }

    // Custom helper getters for business logic & auditing
    public Long getEmployeeId() {
        return employee.getEmployeeId();
    }

    public UUID getStoreId() {
        return employee.getStore() != null ? employee.getStore().getStoreId() : null;
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}