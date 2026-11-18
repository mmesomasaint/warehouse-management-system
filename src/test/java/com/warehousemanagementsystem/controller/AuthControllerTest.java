package com.warehousemanagementsystem.controller;

import tools.jackson.databind.ObjectMapper;
import com.warehousemanagementsystem.security.TokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false) // For pure controller testing
class AuthControllerTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private TokenService tokenService;

    @Test
    void shouldAuthenticateSuccessfullyAndReturnToken() throws Exception {
        // Arrange
        AuthController.LoginRequest request = new AuthController.LoginRequest(
                "manager@warehouse.com",
                "SecurePassword123!"
        );

        Authentication mockAuth = org.mockito.Mockito.mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuth);
        when(tokenService.generateToken(mockAuth)).thenReturn("mock.jwt.access.token");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("mock.jwt.access.token"));
    }

    @Test
    void shouldRejectLoginWithBadCredentials() throws Exception {
        // Arrange
        AuthController.LoginRequest request = new AuthController.LoginRequest(
                "wrong@warehouse.com",
                "BadPassword"
        );

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid username or password"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized()); // Or handled by your global exception handler as 401/400
    }

    @Test
    void shouldReturnBadRequestWhenLoginRequestHasInvalidEmailFormat() throws Exception {
        // Arrange - invalid email string
        AuthController.LoginRequest request = new AuthController.LoginRequest(
                "not-an-email-address",
                "Password123!"
        );

        // Act & Assert - Bean validation (@Email) should catch this immediately
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}