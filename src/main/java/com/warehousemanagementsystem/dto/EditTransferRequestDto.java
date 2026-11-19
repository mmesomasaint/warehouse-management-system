package com.warehousemanagementsystem.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import java.util.UUID;
import java.util.List;

public record EditTransferRequestDto(
    @NotNull UUID transferId, 
    @NotEmpty List<TransferItemRequest> newItems
) {}

