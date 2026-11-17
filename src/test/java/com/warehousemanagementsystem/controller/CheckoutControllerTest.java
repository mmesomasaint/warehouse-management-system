package com.warehousemanagementsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.warehousemanagementsystem.service.CheckoutService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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

    @MockBean
    private CheckoutService checkoutService;

    @Test
    @WithMockUser(authorities = "sales:checkout")
    void shouldReturnBadRequestWhenCheckoutRequestHasMissingFields() throws Exception {
        // Arrange - missing storeId
        CheckoutController.CheckoutRequest invalidRequest = new CheckoutController.CheckoutRequest(
                null,
                1L,
                List.of(new CheckoutService.CartItemDto(UUID.randomUUID(), 1, BigDecimal.TEN))
        );

        // Act & Assert
        mockMvc.perform(post("/api/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}