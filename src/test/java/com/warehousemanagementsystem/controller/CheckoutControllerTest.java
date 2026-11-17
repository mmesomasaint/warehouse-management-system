package com.warehousemanagementsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.warehousemanagementsystem.service.CheckoutService;
import com.warehousemanagementsystem.dto.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CheckoutController.class)
@AutoConfigureMockMvc(addFilters = false) // Bypasses JWT filter for direct controller slice testing
class CheckoutControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CheckoutService checkoutService;

    @Test
    @WithMockUser(authorities = "sales:checkout")
    void shouldReturnBadRequestWhenCheckoutRequestHasMissingFields() throws Exception {
        // Arrange - missing storeId
        CheckoutRequestDto invalidRequest = new CheckoutRequestDto(
                null,
                1L,
                1L,
                List.of(new CartItemDto(UUID.randomUUID(), 1, BigDecimal.TEN))
        );

        // Act & Assert
        mockMvc.perform(post("/api/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}