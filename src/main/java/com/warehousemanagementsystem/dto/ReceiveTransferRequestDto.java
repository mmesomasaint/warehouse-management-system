package com.warehousemanagementsystem.dto;

import java.util.UUID;
import java.util.Map;

public record ReceiveTransferRequestDto(UUID transferId, Map<UUID, Integer> receivedQuantities, Long defaultZoneId) {}
