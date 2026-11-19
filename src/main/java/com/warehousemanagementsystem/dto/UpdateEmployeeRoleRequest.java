package com.warehousemanagementsystem.dto;

import com.warehousemanagementsystem.model.security.EmployeeRole;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

public record UpdateEmployeeRoleRequest(
        @NotNull EmployeeRole newRole,
        @NotBlank String reason
) {}