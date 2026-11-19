package com.warehousemanagementsystem.controller;

import com.warehousemanagementsystem.dto.DeactivateEmployeeRequest;
import com.warehousemanagementsystem.dto.EmployeeAuditLogResponse;
import com.warehousemanagementsystem.dto.EmployeeSummaryResponse;
import com.warehousemanagementsystem.dto.RegisterEmployeeRequest;
import com.warehousemanagementsystem.dto.UpdateEmployeeRoleRequest;
import com.warehousemanagementsystem.dto.ExtraPermissionRequest;
import com.warehousemanagementsystem.service.EmployeeManagementService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
@PreAuthorize("hasAuthority('employee:manage')")
public class EmployeeManagementController {

    private final EmployeeManagementService employeeManagementService;

    public EmployeeManagementController(EmployeeManagementService employeeManagementService) {
        this.employeeManagementService = employeeManagementService;
    }

    @PostMapping
    public ResponseEntity<EmployeeSummaryResponse> registerEmployee(
            @RequestBody RegisterEmployeeRequest request) {
        EmployeeSummaryResponse created = employeeManagementService.registerEmployee(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{employeeId}/role/permissions")
    public ResponseEntity<EmployeeSummaryResponse> addPermissions(
            @PathVariable("employeeId") Long employeeId,
            @RequestBody ExtraPermissionRequest request) {
        EmployeeSummaryResponse updated = employeeManagementService.updatePermissions(
                employeeId,
                request.permissionString(),
                request.reason()
        );
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{employeeId}/role")
    public ResponseEntity<EmployeeSummaryResponse> updateRole(
            @PathVariable("employeeId") Long employeeId,
            @RequestBody UpdateEmployeeRoleRequest request) {
        EmployeeSummaryResponse updated = employeeManagementService.updateRole(
                employeeId,
                request.newRole(),
                request.reason()
        );
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{employeeId}/deactivate")
    public ResponseEntity<EmployeeSummaryResponse> deactivateEmployee(
            @PathVariable("employeeId") Long employeeId,
            @RequestBody DeactivateEmployeeRequest request) {
        EmployeeSummaryResponse deactivated = employeeManagementService.deactivateEmployee(
                employeeId,
                request.reason()
        );
        return ResponseEntity.ok(deactivated);
    }

    @GetMapping("/{employeeId}/audit-logs")
    public ResponseEntity<List<EmployeeAuditLogResponse>> getEmployeeAuditLogs(
            @PathVariable("employeeId") Long employeeId) {
        List<EmployeeAuditLogResponse> history = employeeManagementService.getAuditLogsForEmployee(employeeId);
        return ResponseEntity.ok(history);
    }
}