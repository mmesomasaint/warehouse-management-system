package com.warehousemanagementsystem.security;

import com.warehousemanagementsystem.controller.CheckoutController;
import com.warehousemanagementsystem.controller.AuthController; 
import com.warehousemanagementsystem.service.CheckoutService;
import com.warehousemanagementsystem.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({CheckoutController.class, AuthController.class})
@Import(SecurityConfig.class)
class SecurityFilterChainTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private TokenService tokenService;

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
    void shouldAllowUnauthenticatedAccessToAuthEndpoints() throws Exception {
        when(authenticationManager.authenticate(any()))
                .thenReturn(new UsernamePasswordAuthenticationToken("test@warehouse.com", null, List.of()));
        when(tokenService.generateToken(any())).thenReturn("mock-jwt-token");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@warehouse.com\", \"password\":\"password\"}"))
                .andExpect(status().isOk()); // Or isBadRequest/isUnauthorized depending on mock setup
    }

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
