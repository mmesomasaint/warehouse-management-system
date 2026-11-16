package com.warehousemanagementsystem.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.warehousemanagementsystem.service.CheckoutService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

@WebMvcTest(CheckoutController.class)
public class CheckoutControllerTest {
    @Autowired 
    private MockMvc mockMvc;

    @MockitoBean
    private CheckoutService checkoutService;

    @Test 
    void shouldReturn200WhenRequestIsValid() throws Exception {
        CheckoutResponseDto response = CheckoutResponseDto.builder()
            .orderId(UUID.randomUUID())
            .store(new Store())
            .Customer(new Customer())
            .Employee(new Employee())
            .build();

        when(checkoutService.processCheckout(any(CheckoutRequestDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/products")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                    "name": "Laptop",
                    "price": 900.00
                }
                """
            ))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1L));
    }
}
