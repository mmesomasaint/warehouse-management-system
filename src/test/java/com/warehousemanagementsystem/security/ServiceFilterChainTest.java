package com.warehousemanagementsystem.security;

import com.warehousemanagementsystem.controller.CheckoutController;
import com.warehousemanagementsystem.service.CheckoutService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CheckoutController.class)
class SecurityFilterChainTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CheckoutService checkoutService;

    @Test
    void shouldRejectUnauthenticatedRequestsToSecuredEndpoints() throws Exception {
        mockMvc.perform(post("/api/checkout"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "sales:checkout")
    void shouldAllowAuthenticatedUserWithCorrectAuthority() throws Exception {
        mockMvc.perform(post("/api/checkout")
                .with(jwt().authorities(new SimpleGrantedAuthority("sales:checkout"))))
            .andExpect(status().isBadRequest());
    }
}