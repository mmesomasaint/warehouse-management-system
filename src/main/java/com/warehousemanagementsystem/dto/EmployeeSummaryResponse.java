package com.warehousemanagementsystem.dto;

import com.warehousemanagementsystem.model.security.EmployeeRole;

import lombok.*;

import java.util.UUID;
import java.util.Set;

@Builder 
public record EmployeeSummaryResponse(
    Long employeeId,
    String email,
    String fullName,
    UUID storeId,
    boolean active,
    EmployeeRole role,
    Set<String> extraPermissions
) {}