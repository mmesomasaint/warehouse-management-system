package com.warehousemanagementsystem.dto;

import java.util.UUID;
import java.util.List;

public record CheckoutRequestDto (UUID storeId, Long customerId, Long employeeId, List<CartItemDto> cartItems) {}