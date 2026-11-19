package com.warehousemanagementsystem.controller;

import com.warehousemanagementsystem.config.SecurityConfig;
import com.warehousemanagementsystem.dto.EmployeeAuditLogResponse;
import com.warehousemanagementsystem.model.EmployeeAuditAction;
import com.warehousemanagementsystem.security.RsaKeyProperties;
import com.warehousemanagementsystem.service.EmployeeManagementService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print; // REM: debug

@WebMvcTest(EmployeeManagementController.class)
@Import({
        SecurityConfig.class,
        EmployeeAuditLogsControllerTest.TestKeyConfig.class,
        EmployeeAuditLogsControllerTest.TestExceptionHandler.class
})
class EmployeeAuditLogsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmployeeManagementService employeeManagementService;

    @Test
    void shouldReturn401WhenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/employees/{employeeId}/audit-logs", 42L))
                .andExpect(status().isUnauthorized());

        verify(employeeManagementService, never()).getAuditLogsForEmployee(any());
    }

    @Test
    void shouldReturn403WhenAuthenticatedWithoutEmployeeManageAuthority() throws Exception {
        mockMvc.perform(get("/api/employees/{employeeId}/audit-logs", 42L)
                        .with(jwt().authorities(new SimpleGrantedAuthority("sales:checkout"))))
                .andDo(print())
                .andExpect(status().isForbidden());

        verify(employeeManagementService, never()).getAuditLogsForEmployee(any());
    }

    @Test
    void shouldReturn404WhenEmployeeDoesNotExist() throws Exception {
        when(employeeManagementService.getAuditLogsForEmployee(999L))
                .thenThrow(new EntityNotFoundException("Employee not found: 999"));

        mockMvc.perform(get("/api/employees/{employeeId}/audit-logs", 999L)
                        .with(jwt().authorities(new SimpleGrantedAuthority("employee:manage"))))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Employee not found: 999"));
    }

    @Test
    void shouldReturn200WithAuditLogsWhenAuthorized() throws Exception {
        UUID auditId = UUID.randomUUID();
        OffsetDateTime occurredAt = OffsetDateTime.parse("2026-11-19T10:45:00Z");

        EmployeeAuditLogResponse entry = new EmployeeAuditLogResponse(
                auditId,
                42L,
                "staff@warehouse.com",
                EmployeeAuditAction.CREATED,
                99L,
                "Initial onboarding",
                "Registered employee with roles [STORE_ASSOCIATE]",
                occurredAt
        );

        when(employeeManagementService.getAuditLogsForEmployee(42L))
                .thenReturn(List.of(entry));

        mockMvc.perform(get("/api/employees/{employeeId}/audit-logs", 42L)
                        .with(jwt().authorities(new SimpleGrantedAuthority("employee:manage"))))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].auditId").value(auditId.toString()))
                .andExpect(jsonPath("$[0].targetEmployeeId").value(42L))
                .andExpect(jsonPath("$[0].targetEmail").value("staff@warehouse.com"))
                .andExpect(jsonPath("$[0].action").value("CREATED"))
                .andExpect(jsonPath("$[0].actorEmployeeId").value(99L))
                .andExpect(jsonPath("$[0].reason").value("Initial onboarding"));
    }

    @TestConfiguration
    static class TestKeyConfig {
        @Bean
        @Primary
        RsaKeyProperties rsaKeyProperties() throws Exception {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair pair = generator.generateKeyPair();
            return new RsaKeyProperties(
                    (RSAPublicKey) pair.getPublic(),
                    (RSAPrivateKey) pair.getPrivate(),
                    60
            );
        }
    }

    @RestControllerAdvice
    static class TestExceptionHandler {
        @ExceptionHandler(EntityNotFoundException.class)
        ResponseEntity<Map<String, String>> handleNotFound(EntityNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Not Found", "message", ex.getMessage()));
        }
    }
}