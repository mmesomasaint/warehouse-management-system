package com.warehousemanagementsystem.security;

import com.warehousemanagementsystem.controller.CheckoutController;
import com.warehousemanagementsystem.service.CheckoutService;
import com.warehousemanagementsystem.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CheckoutController.class)
@Import(SecurityConfig.class)
class SecurityFilterChainTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CheckoutService checkoutService;

    @MockitoBean
    private RsaKeyProperties rsaKeys;

    // Overrides SecurityConfig's @Bean methods so Nimbus doesn't fail on null RSA keys
    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private JwtEncoder jwtEncoder;

    @Test
    void shouldRejectUnauthenticatedRequestsToSecuredEndpoints() throws Exception {
        mockMvc.perform(post("/api/checkout"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowAuthenticatedUserWithCorrectAuthority() throws Exception {
        mockMvc.perform(post("/api/checkout")
                .with(jwt().authorities(new SimpleGrantedAuthority("sales:checkout")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest());
    }
}
