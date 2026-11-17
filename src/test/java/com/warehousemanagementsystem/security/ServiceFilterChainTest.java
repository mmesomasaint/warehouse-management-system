package com.warehousemanagementsystem.security;

import com.warehousemanagementsystem.controller.CheckoutController;
import com.warehousemanagementsystem.service.CheckoutService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CheckoutController.class)
class SecurityFilterChainTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CheckoutService checkoutService;

    @Test
    void shouldRejectUnauthenticatedRequestsToSecuredEndpoints() throws Exception {
        // Act & Assert - No user logged in, expect 401 Unauthorized (or 403 depending on filter chain config)
        mockMvc.perform(post("/api/checkout"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "sales:checkout")
    void shouldAllowAuthenticatedUserWithCorrectAuthority() throws Exception {
        // Act & Assert - User has 'sales:checkout' permission, service mock handles execution
        // (Assuming request body structure is validated or bypassed for test simplicity)
        mockMvc.perform(post("/api/checkout"))
                .andExpect(status().isBadRequest()); // Fails at request body validation (400), proving security passed!
    }
}