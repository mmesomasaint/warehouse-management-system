package com.warehousemanagementsystem.dto;

import java.util.UUID;

public record TransferItemRequest(UUID productId, int quantityShipped) {}
