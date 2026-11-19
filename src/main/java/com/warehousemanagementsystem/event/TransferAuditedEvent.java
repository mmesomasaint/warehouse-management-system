package com.warehousemanagementsystem.event;

import com.warehousemanagementsystem.model.EditorType;
import com.warehousemanagementsystem.model.TransferAuditAction;

import java.time.OffsetDateTime;
import java.util.UUID;

public record TransferAuditedEvent(
        UUID transferId,
        TransferAuditAction action,
        Long actorEmployeeId,
        EditorType editorType,
        String reason,
        String details,
        OffsetDateTime occurredAt
) {}