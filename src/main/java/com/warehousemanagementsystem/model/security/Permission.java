package com.warehousemanagementsystem.model.security;

public enum Permission {
    // Sales & Checkout
    SALES_CHECKOUT("sales:checkout"),
    
    // Inventory & Stock
    INVENTORY_READ("inventory:read"),
    INVENTORY_WRITE("inventory:write"),
    
    // Logistics & Transfers
    TRANSFER_CREATE("transfer:create"),
    TRANSFER_SHIP("transfer:ship"),
    TRANSFER_RECEIVE("transfer:receive"),
    
    // People & HR
    EMPLOYEE_READ("employee:read"),
    EMPLOYEE_WRITE("employee:write"),
    PAYROLL_APPROVE("payroll:approve");

    private final String permission;

    Permission(String permission) {
        this.permission = permission;
    }

    public String getPermission() {
        return permission;
    }
}