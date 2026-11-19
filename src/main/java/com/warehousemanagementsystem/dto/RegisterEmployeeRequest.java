package com.warehousemanagementsystem.dto;

import com.warehousemanagementsystem.model.security.EmployeeRole;

import java.util.UUID;

public record RegisterEmployeeRequest(
    String email,
    String fullName,
    String rawPassword,
    UUID storeId,
    EmployeeRole role
) {}