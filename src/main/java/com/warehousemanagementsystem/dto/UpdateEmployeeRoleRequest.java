package com.warehousemanagementsystem.dto;

import com.warehousemanagementsystem.model.security.EmployeeRole;

public record UpdateEmployeeRoleRequest(
        EmployeeRole newRole,
        String reason
) {}