package com.warehousemanagementsystem.model.security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Set;
import java.util.stream.Collectors;

public enum EmployeeRole {
    
    CASHIER(Set.of(
            Permission.SALES_CHECKOUT,
            Permission.INVENTORY_READ
    )),

    FLOOR_SALES(Set.of(
            Permission.SALES_CHECKOUT,
            Permission.INVENTORY_READ,
            Permission.TRANSFER_CREATE
    )),

    LOGISTICS_COORDINATOR(Set.of(
            Permission.INVENTORY_READ,
            Permission.INVENTORY_WRITE,
            Permission.TRANSFER_CREATE,
            Permission.TRANSFER_SHIP,
            Permission.TRANSFER_RECEIVE
    )),

    STORE_MANAGER(Set.of(
            Permission.SALES_CHECKOUT,
            Permission.INVENTORY_READ,
            Permission.INVENTORY_WRITE,
            Permission.TRANSFER_CREATE,
            Permission.TRANSFER_SHIP,
            Permission.TRANSFER_RECEIVE,
            Permission.EMPLOYEE_READ,
            Permission.EMPLOYEE_WRITE
    )),

    ADMIN(Set.of(
            Permission.SALES_CHECKOUT,
            Permission.INVENTORY_READ,
            Permission.INVENTORY_WRITE,
            Permission.TRANSFER_CREATE,
            Permission.TRANSFER_SHIP,
            Permission.TRANSFER_RECEIVE,
            Permission.EMPLOYEE_READ,
            Permission.EMPLOYEE_WRITE,
            Permission.PAYROLL_APPROVE
    ));

    private final Set<Permission> permissions;

    EmployeeRole(Set<Permission> permissions) {
        this.permissions = permissions;
    }

    public Set<Permission> getPermissions() {
        return permissions;
    }

    /**
     * Converts role and permissions into Spring Security authorities.
     * Generates: "ROLE_STORE_MANAGER" and individual permission strings like "inventory:read"
     */
    public Set<SimpleGrantedAuthority> getAuthorities() {
        Set<SimpleGrantedAuthority> authorities = permissions.stream()
                .map(permission -> new SimpleGrantedAuthority(permission.getPermission()))
                .collect(Collectors.toSet());
        
        authorities.add(new SimpleGrantedAuthority("ROLE_" + this.name()));
        return authorities;
    }
}