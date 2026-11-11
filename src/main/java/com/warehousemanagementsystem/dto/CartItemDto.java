package com.warehousemanagementsystem.dto;

import java.util.UUID;
import java.math.BigDecimal;

public record CartItemDto(UUID productId, int quantity, BigDecimal unitPrice) {}