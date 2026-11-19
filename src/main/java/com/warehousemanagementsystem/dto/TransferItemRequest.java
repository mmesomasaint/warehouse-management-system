package com.warehousemanagementsystem.dto;

import java.util.UUID;
import jakarta.validation.constraints.*;;

public record TransferItemRequest(@NotNull UUID productId, @NotNull int quantityShipped) {}
