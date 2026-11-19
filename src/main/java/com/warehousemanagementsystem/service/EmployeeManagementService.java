package com.warehousemanagementsystem.service;

import com.warehousemanagementsystem.dto.EmployeeAuditLogResponse;
import com.warehousemanagementsystem.dto.EmployeeSummaryResponse;
import com.warehousemanagementsystem.dto.RegisterEmployeeRequest;
import com.warehousemanagementsystem.model.*;
import com.warehousemanagementsystem.model.security.EmployeeRole;
import com.warehousemanagementsystem.repository.EmployeeAuditLogRepository;
import com.warehousemanagementsystem.repository.EmployeeRepository;
import com.warehousemanagementsystem.repository.StoreRepository;
import com.warehousemanagementsystem.security.EmployeeContextService;
import jakarta.persistence.EntityNotFoundException;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.Set;
import java.util.List;

@Service
public class EmployeeManagementService {

    private final EmployeeRepository employeeRepository;
    private final StoreRepository storeRepository;
    private final EmployeeAuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmployeeContextService employeeContextService;

    public EmployeeManagementService(EmployeeRepository employeeRepository,
                                   StoreRepository storeRepository,
                                   EmployeeAuditLogRepository auditLogRepository,
                                   PasswordEncoder passwordEncoder,
                                   EmployeeContextService employeeContextService) {
        this.employeeRepository = employeeRepository;
        this.storeRepository = storeRepository;
        this.auditLogRepository = auditLogRepository;
        this.passwordEncoder = passwordEncoder;
        this.employeeContextService = employeeContextService;
    }

    @Transactional
    public EmployeeSummaryResponse registerEmployee(RegisterEmployeeRequest request) {
        if (request.email() == null || request.email().isBlank()) {
            throw new IllegalArgumentException("Employee email is required.");
        }
        if (request.rawPassword() == null || request.rawPassword().isBlank()) {
            throw new IllegalArgumentException("Initial password is required.");
        }
        if (employeeRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalStateException("An employee with email already exists: " + request.email());
        }

        Store store = null;
        if (request.storeId() != null) {
            store = storeRepository.findById(request.storeId())
                    .orElseThrow(() -> new EntityNotFoundException("Store not found: " + request.storeId()));
        }

        Long actorId = employeeContextService.getCurrentEmployeeId();
        OffsetDateTime now = OffsetDateTime.now();

        Employee employee = new Employee();
        employee.setEmail(request.email().trim().toLowerCase());
        employee.setFullName(request.fullName());
        employee.setPasswordHash(passwordEncoder.encode(request.rawPassword()));
        employee.setStore(store);
        employee.setActive(true);
        employee.setRole(request.role());

        Employee saved = employeeRepository.save(employee);

        auditLogRepository.save(new EmployeeAuditLog(
                saved.getEmployeeId(),
                saved.getEmail(),
                EmployeeAuditAction.CREATED,
                actorId,
                "Initial onboarding",
                "Registered employee with role " + request.role()
                        + (store != null ? " at store " + store.getStoreId() : " (no store assigned)"),
                now
        ));

        return toSummaryResponse(saved);
    }

    @Transactional
    @PreAuthorize ("hasAuthority('employee:manage')")
    public EmployeeSummaryResponse updatePermissions(Long targetEmployeeId, String newPermissionString, String reason) {
        Long actorId = employeeContextService.getCurrentEmployeeId();
        OffsetDateTime now = OffsetDateTime.now();

        Employee employee = employeeRepository.findById(targetEmployeeId)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found: " + targetEmployeeId));

        Set<String> permissionSet = employee.getExtraPermissions();
        permissionSet.add(newPermissionString);
        employee.setExtraPermissions(permissionSet);
        Employee saved = employeeRepository.save(employee);

        auditLogRepository.save(new EmployeeAuditLog(
            saved.getEmployeeId(),
            saved.getEmail(),
            EmployeeAuditAction.EXTRA_PERMISSION,
            actorId,
            reason,
            "Added new extra permission " + newPermissionString + " to an employee",
            now
        ));

        return toSummaryResponse(saved);
    }

    @Transactional
    public EmployeeSummaryResponse updateRole(Long targetEmployeeId, EmployeeRole newRole, String reason) {
        Long actorId = employeeContextService.getCurrentEmployeeId();
        OffsetDateTime now = OffsetDateTime.now();

        Employee employee = employeeRepository.findById(targetEmployeeId)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found: " + targetEmployeeId));
        
        EmployeeRole oldRole = employee.getRole();
        if (oldRole.equals(newRole)) 
            throw new IllegalArgumentException("This Employee already has this Role" + newRole);

        employee.setRole(newRole);
        Employee saved = employeeRepository.save(employee);

        auditLogRepository.save(new EmployeeAuditLog(
            saved.getEmployeeId(),
            saved.getEmail(),
            EmployeeAuditAction.ROLES_UPDATED,
            actorId,
            reason,
            "Changed employees role from " + oldRole + " to " + newRole,
            now
        ));

        return toSummaryResponse(saved);
    }

    @Transactional
    public EmployeeSummaryResponse deactivateEmployee(Long targetEmployeeId, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Deactivation reason is required.");
        }

        Long actorId = employeeContextService.getCurrentEmployeeId();
        if (actorId != null && actorId.equals(targetEmployeeId)) {
            throw new IllegalStateException("An administrator cannot deactivate their own account.");
        }

        OffsetDateTime now = OffsetDateTime.now();

        Employee employee = employeeRepository.findById(targetEmployeeId)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found: " + targetEmployeeId));

        if (!employee.isActive()) {
            throw new IllegalStateException("Employee is already inactive: " + targetEmployeeId);
        }

        employee.setActive(false);
        Employee saved = employeeRepository.save(employee);

        auditLogRepository.save(new EmployeeAuditLog(
            saved.getEmployeeId(),
            saved.getEmail(),
            EmployeeAuditAction.DEACTIVATED,
            actorId,
            reason,
            "Deactivated employee account (historical records preserved)",
            now
        ));

        return toSummaryResponse(saved);
    }
    
    @Transactional(readOnly = true)
    public List<EmployeeAuditLogResponse> getAuditLogsForEmployee(Long targetEmployeeId) {
        if (!employeeRepository.existsById(targetEmployeeId)) {
            throw new EntityNotFoundException("Employee not found: " + targetEmployeeId);
        }

        return auditLogRepository.findByTargetEmployeeIdOrderByOccurredAtDesc(targetEmployeeId)
                .stream()
                .map(EmployeeAuditLogResponse::from)
                .toList();
    }

    private EmployeeSummaryResponse toSummaryResponse(Employee employee) {
        UUID assignedStoreId = employee.getStore() != null ? employee.getStore().getStoreId() : null;
        return EmployeeSummaryResponse.builder()
            .employeeId(employee.getEmployeeId())
            .email(employee.getEmail())
            .fullName(employee.getFullName())
            .storeId(assignedStoreId)
            .active(employee.isActive())
            .role(employee.getRole())
            .extraPermissions(employee.getExtraPermissions())
            .build();
    }
}