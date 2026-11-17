package com.warehousemanagementsystem.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import java.util.List;

public record CreateTransferRequestDto (
    UUID sourceRdcId, 
    UUID sourceStoreId, 
    @NotNull UUID destinationStoreId, 
    @NotEmpty List<TransferItemRequest> items
) {}
