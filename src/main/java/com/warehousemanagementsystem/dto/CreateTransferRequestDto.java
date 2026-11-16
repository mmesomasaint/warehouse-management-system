package com.warehousemanagementsystem.dto;

import java.util.UUID;
import java.util.List;

public record CreateTransferRequestDto (UUID sourceRdcId, UUID sourceStoreId, UUID destinationStoreId, List<TransferItemRequest> items) {}
