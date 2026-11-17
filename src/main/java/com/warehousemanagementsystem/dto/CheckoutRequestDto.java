package com.warehousemanagementsystem.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import java.util.List;

public record CheckoutRequestDto (
    @NotNull UUID storeId, 
    Long customerId, 
    Long employeeId, 
    @NotEmpty List<CartItemDto> cartItems
) {}