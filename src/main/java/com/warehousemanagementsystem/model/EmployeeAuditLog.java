package com.warehousemanagementsystem.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "employee_audit_logs")
public class EmployeeAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "audit_id", nullable = false, updatable = false)
    private UUID auditId;

    @Column(name = "target_employee_id", nullable = false, updatable = false)
    private Long targetEmployeeId;

    @Column(name = "target_email", nullable = false, updatable = false)
    private String targetEmail;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, updatable = false, length = 32)
    private EmployeeAuditAction action;

    @Column(name = "actor_employee_id", updatable = false)
    private Long actorEmployeeId;

    @Column(name = "reason", updatable = false, length = 500)
    private String reason;

    @Column(name = "details", updatable = false, length = 1500)
    private String details;

    @Column(name = "occurred_at", nullable = false, updatable = false)
    private OffsetDateTime occurredAt;

    protected EmployeeAuditLog() {
    }

    public EmployeeAuditLog(Long targetEmployeeId,
                            String targetEmail,
                            EmployeeAuditAction action,
                            Long actorEmployeeId,
                            String reason,
                            String details,
                            OffsetDateTime occurredAt) {
        this.targetEmployeeId = targetEmployeeId;
        this.targetEmail = targetEmail;
        this.action = action;
        this.actorEmployeeId = actorEmployeeId;
        this.reason = reason;
        this.details = details;
        this.occurredAt = occurredAt;
    }

    public UUID getAuditId() {
        return auditId;
    }

    public Long getTargetEmployeeId() {
        return targetEmployeeId;
    }

    public String getTargetEmail() {
        return targetEmail;
    }

    public EmployeeAuditAction getAction() {
        return action;
    }

    public Long getActorEmployeeId() {
        return actorEmployeeId;
    }

    public String getReason() {
        return reason;
    }

    public String getDetails() {
        return details;
    }

    public OffsetDateTime getOccurredAt() {
        return occurredAt;
    }
}