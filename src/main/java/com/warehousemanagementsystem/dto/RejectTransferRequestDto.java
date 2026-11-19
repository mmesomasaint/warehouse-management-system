package com.warehousemanagementsystem.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record RejectTransferRequestDto(
    @NotNull UUID transferId, 
    @NotBlank String rejectionReason
) {}
