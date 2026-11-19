package com.warehousemanagementsystem.dto;

import jakarta.validation.constraints.NotBlank;

public record ExtraPermissionRequest (
        @NotBlank String permissionString,
        @NotBlank String reason
) {}