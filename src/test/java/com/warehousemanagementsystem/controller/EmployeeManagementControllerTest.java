package com.warehousemanagementsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.warehousemanagementsystem.config.SecurityConfig;
import com.warehousemanagementsystem.dto.DeactivateEmployeeRequest;
import com.warehousemanagementsystem.dto.EmployeeSummaryResponse;
import com.warehousemanagementsystem.dto.RegisterEmployeeRequest;
import com.warehousemanagementsystem.dto.UpdateEmployeeRoleRequest;
import com.warehousemanagementsystem.dto.ExtraPermissionRequest;
import com.warehousemanagementsystem.model.security.EmployeeRole;
import com.warehousemanagementsystem.security.RsaKeyProperties;
import com.warehousemanagementsystem.service.EmployeeManagementService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmployeeManagementController.class)
@Import({SecurityConfig.class, EmployeeManagementControllerTest.TestKeyConfig.class})
class EmployeeManagementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EmployeeManagementService employeeManagementService;

    @Test
    void shouldReturn401WhenUnauthenticated() throws Exception {
        RegisterEmployeeRequest request = new RegisterEmployeeRequest(
            "new.hire@warehouse.com",
            "Kelechi Nnamdi",
            "TempPass123!",
            UUID.randomUUID(),
            EmployeeRole.CASHIER
        );

        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn403WhenAuthenticatedWithoutEmployeeManageAuthority() throws Exception {
        RegisterEmployeeRequest request = new RegisterEmployeeRequest(
            "new.hire@warehouse.com",
            "Kelechi Nnamdi",
            "TempPass123!",
            UUID.randomUUID(),
            EmployeeRole.CASHIER
        );

        mockMvc.perform(post("/api/employees")
                .with(jwt().authorities(new SimpleGrantedAuthority("sales:checkout")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturn201WhenRegisteringEmployeeWithEmployeeManageAuthority() throws Exception {
        UUID storeId = UUID.randomUUID();
        RegisterEmployeeRequest request = new RegisterEmployeeRequest(
            "new.hire@warehouse.com",
            "Kelechi Nnamdi",
            "TempPass123!",
            storeId,
            EmployeeRole.CASHIER
        );

        EmployeeSummaryResponse expected = EmployeeSummaryResponse.builder()
            .employeeId(42L)
            .email("new.hire@warehouse.com")
            .fullName("Kelechi Nnamdi")
            .storeId(storeId)
            .active(true)
            .role(EmployeeRole.CASHIER)
            .build();

        when(employeeManagementService.registerEmployee(any(RegisterEmployeeRequest.class)))
                .thenReturn(expected);

        mockMvc.perform(post("/api/employees")
                .with(jwt().authorities(new SimpleGrantedAuthority("employee:manage")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.employeeId").value(42L))
            .andExpect(jsonPath("$.email").value("new.hire@warehouse.com"))
            .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void shouldReturn200WhenUpdatingRolesWithEmployeeManageAuthority() throws Exception {
        UUID storeId = UUID.randomUUID();
        UpdateEmployeeRoleRequest request = new UpdateEmployeeRoleRequest(
                EmployeeRole.STORE_MANAGER,
                "Promoted to store manager"
        );

        EmployeeSummaryResponse expected = EmployeeSummaryResponse.builder()
            .employeeId(42L)
            .email("new.hire@warehouse.com")
            .fullName("Kelechi Nnamdi")
            .storeId(storeId)
            .active(true)
            .role(EmployeeRole.STORE_MANAGER)
            .build();

        when(employeeManagementService.updateRole(eq(42L), eq(EmployeeRole.STORE_MANAGER), eq("Promoted to store manager")))
                .thenReturn(expected);

        mockMvc.perform(put("/api/employees/{employeeId}/role", 42L)
                        .with(jwt().authorities(new SimpleGrantedAuthority("employee:manage")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeId").value(42L))
                .andExpect(jsonPath("$.roles[0]").value("STORE_MANAGER"));
    }

    @Test
    void shouldReturn200WhenDeactivatingEmployeeWithEmployeeManageAuthority() throws Exception {
        UUID storeId = UUID.randomUUID();
        DeactivateEmployeeRequest request = new DeactivateEmployeeRequest("End of seasonal shift");

        EmployeeSummaryResponse expected = EmployeeSummaryResponse.builder()
            .employeeId(42L)
            .email("new.hire@warehouse.com")
            .fullName("Kelechi Nnamdi")
            .storeId(storeId)
            .active(true)
            .role(EmployeeRole.HR_MANAGER)
            .build();

        when(employeeManagementService.deactivateEmployee(eq(42L), eq("End of seasonal shift")))
                .thenReturn(expected);

        mockMvc.perform(post("/api/employees/{employeeId}/deactivate", 42L)
                        .with(jwt().authorities(new SimpleGrantedAuthority("employee:manage")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeId").value(42L))
                .andExpect(jsonPath("$.active").value(false));
    }

    @org.springframework.boot.test.context.TestConfiguration
    static class TestKeyConfig {
        @org.springframework.context.annotation.Bean
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
}