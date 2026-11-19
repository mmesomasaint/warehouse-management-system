package com.warehousemanagementsystem.dto;

import com.warehousemanagementsystem.model.EmployeeAuditAction;
import com.warehousemanagementsystem.model.EmployeeAuditLog;

import java.time.OffsetDateTime;
import java.util.UUID;

public record EmployeeAuditLogResponse(
        UUID auditId,
        Long targetEmployeeId,
        String targetEmail,
        EmployeeAuditAction action,
        Long actorEmployeeId,
        String reason,
        String details,
        OffsetDateTime occurredAt
) {
    public static EmployeeAuditLogResponse from(EmployeeAuditLog log) {
        return new EmployeeAuditLogResponse(
                log.getAuditId(),
                log.getTargetEmployeeId(),
                log.getTargetEmail(),
                log.getAction(),
                log.getActorEmployeeId(),
                log.getReason(),
                log.getDetails(),
                log.getOccurredAt()
        );
    }
}