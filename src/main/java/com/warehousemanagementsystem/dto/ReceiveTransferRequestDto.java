package com.warehousemanagementsystem.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import java.util.UUID;
import java.util.Map;

public record ReceiveTransferRequestDto(
    @NotNull UUID transferId, 
    @NotEmpty Map<UUID, Integer> receivedQuantities, 
    @NotNull Long defaultZoneId
) {}
