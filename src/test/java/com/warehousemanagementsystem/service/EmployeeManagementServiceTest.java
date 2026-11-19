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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeManagementServiceTest {

    @Mock private EmployeeRepository employeeRepository;
    @Mock private StoreRepository storeRepository;
    @Mock private EmployeeAuditLogRepository auditLogRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private EmployeeContextService employeeContextService;
    private EmployeeManagementService employeeManagementService;

    private UUID storeId;
    private Store mockStore;

    @BeforeEach
    void setUp() {
        employeeManagementService = new EmployeeManagementService(
                employeeRepository,
                storeRepository,
                auditLogRepository,
                passwordEncoder,
                employeeContextService
        );

        storeId = UUID.randomUUID();
        mockStore = new Store();
        mockStore.setStoreId(storeId);
    }

    @Test
    void shouldRegisterEmployeeAndWriteCreatedAuditLog() {
        RegisterEmployeeRequest request = new RegisterEmployeeRequest(
                "Associate.One@Warehouse.com",
                "Adaeze Obi",
                "TempPass123!",
                storeId,
                EmployeeRole.LOGISTICS_COORDINATOR
        );

        when(employeeRepository.findByEmail("Associate.One@Warehouse.com")).thenReturn(Optional.empty());
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(mockStore));
        when(passwordEncoder.encode("TempPass123!")).thenReturn("encoded-hash");
        when(employeeContextService.getCurrentEmployeeId()).thenReturn(99L);
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> {
            Employee emp = invocation.getArgument(0);
            emp.setEmployeeId(10L);
            return emp;
        });

        EmployeeSummaryResponse response = employeeManagementService.registerEmployee(request);

        assertThat(response.employeeId()).isEqualTo(10L);
        assertThat(response.email()).isEqualTo("associate.one@warehouse.com");
        assertThat(response.fullName()).isEqualTo("Adaeze Obi");
        assertThat(response.storeId()).isEqualTo(storeId);
        assertThat(response.active()).isTrue();
        assertThat(response.role()).isEqualTo(EmployeeRole.LOGISTICS_COORDINATOR);

        ArgumentCaptor<EmployeeAuditLog> auditCaptor = ArgumentCaptor.forClass(EmployeeAuditLog.class);
        verify(auditLogRepository).save(auditCaptor.capture());

        EmployeeAuditLog savedLog = auditCaptor.getValue();
        assertThat(savedLog.getTargetEmployeeId()).isEqualTo(10L);
        assertThat(savedLog.getTargetEmail()).isEqualTo("associate.one@warehouse.com");
        assertThat(savedLog.getAction()).isEqualTo(EmployeeAuditAction.CREATED);
        assertThat(savedLog.getActorEmployeeId()).isEqualTo(99L);
        assertThat(savedLog.getOccurredAt()).isNotNull();
    }

    @Test
    void shouldUpdateRolesAndWriteRolesUpdatedAuditLog() {
        Employee existing = new Employee();
        existing.setEmployeeId(10L);
        existing.setEmail("staff@warehouse.com");
        existing.setFullName("Chinedu Eze");
        existing.setStore(mockStore);
        existing.setActive(true);
        existing.setRole(EmployeeRole.CASHIER);

        when(employeeContextService.getCurrentEmployeeId()).thenReturn(99L);
        when(employeeRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EmployeeSummaryResponse response = employeeManagementService.updatePermissions(
                10L,
                "employee:manage",
                "Promoted to shift supervisor"
        );

        assertThat(response.extraPermissions()).containsExactly("employee:manage");

        ArgumentCaptor<EmployeeAuditLog> auditCaptor = ArgumentCaptor.forClass(EmployeeAuditLog.class);
        verify(auditLogRepository).save(auditCaptor.capture());

        EmployeeAuditLog log = auditCaptor.getValue();
        assertThat(log.getAction()).isEqualTo(EmployeeAuditAction.ROLES_UPDATED);
        assertThat(log.getActorEmployeeId()).isEqualTo(99L);
        assertThat(log.getReason()).isEqualTo("Promoted to shift supervisor");
        assertThat(log.getDetails()).contains("STORE_ASSOCIATE").contains("STORE_MANAGER");
    }

    @Test
    void shouldPreventSelfDeactivation() {
        when(employeeContextService.getCurrentEmployeeId()).thenReturn(99L);

        assertThatThrownBy(() -> employeeManagementService.deactivateEmployee(99L, "Leaving"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot deactivate their own account");

        verify(employeeRepository, never()).save(any());
        verify(auditLogRepository, never()).save(any());
    }

    @Test
    void shouldDeactivateEmployeeAndWriteDeactivatedAuditLog() {
        Employee target = new Employee();
        target.setEmployeeId(25L);
        target.setEmail("former.staff@warehouse.com");
        target.setFullName("Ngozi Okafor");
        target.setStore(mockStore);
        target.setActive(true);
        target.setRole(EmployeeRole.LOGISTICS_COORDINATOR);

        when(employeeContextService.getCurrentEmployeeId()).thenReturn(99L);
        when(employeeRepository.findById(25L)).thenReturn(Optional.of(target));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EmployeeSummaryResponse response = employeeManagementService.deactivateEmployee(25L, "End of contract");

        assertThat(response.active()).isFalse();

        ArgumentCaptor<EmployeeAuditLog> auditCaptor = ArgumentCaptor.forClass(EmployeeAuditLog.class);
        verify(auditLogRepository).save(auditCaptor.capture());

        EmployeeAuditLog log = auditCaptor.getValue();
        assertThat(log.getTargetEmployeeId()).isEqualTo(25L);
        assertThat(log.getAction()).isEqualTo(EmployeeAuditAction.DEACTIVATED);
        assertThat(log.getActorEmployeeId()).isEqualTo(99L);
        assertThat(log.getReason()).isEqualTo("End of contract");
    }

    @Test
    void shouldReturnOrderedAuditLogsForExistingEmployee() {
        OffsetDateTime newerTime = OffsetDateTime.parse("2026-11-19T10:45:00Z");
        OffsetDateTime olderTime = OffsetDateTime.parse("2026-11-18T08:15:00Z");

        EmployeeAuditLog deactivatedLog = new EmployeeAuditLog(
                42L,
                "staff@warehouse.com",
                EmployeeAuditAction.DEACTIVATED,
                99L,
                "End of seasonal shift",
                "Deactivated employee account",
                newerTime
        );
        EmployeeAuditLog createdLog = new EmployeeAuditLog(
                42L,
                "staff@warehouse.com",
                EmployeeAuditAction.CREATED,
                99L,
                "Initial onboarding",
                "Registered employee with roles [STORE_ASSOCIATE]",
                olderTime
        );

        when(employeeRepository.existsById(42L)).thenReturn(true);
        when(auditLogRepository.findByTargetEmployeeIdOrderByOccurredAtDesc(42L))
                .thenReturn(List.of(deactivatedLog, createdLog));

        List<EmployeeAuditLogResponse> result = employeeManagementService.getAuditLogsForEmployee(42L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).action()).isEqualTo(EmployeeAuditAction.DEACTIVATED);
        assertThat(result.get(0).actorEmployeeId()).isEqualTo(99L);
        assertThat(result.get(0).reason()).isEqualTo("End of seasonal shift");
        assertThat(result.get(1).action()).isEqualTo(EmployeeAuditAction.CREATED);
        verify(auditLogRepository).findByTargetEmployeeIdOrderByOccurredAtDesc(42L);
    }

    @Test
    void shouldThrowEntityNotFoundWhenFetchingAuditLogsForUnknownEmployee() {
        when(employeeRepository.existsById(404L)).thenReturn(false);

        assertThatThrownBy(() -> employeeManagementService.getAuditLogsForEmployee(404L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("404");

        verify(auditLogRepository, never()).findByTargetEmployeeIdOrderByOccurredAtDesc(any());
    }
}