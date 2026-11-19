package com.warehousemanagementsystem.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record RejectTransferRequestDto(
    @NotNull UUID transferId, 
    @NotNull String rejectionReason
) {}
