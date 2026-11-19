package com.warehousemanagementsystem.repository;

import com.warehousemanagementsystem.model.EmployeeAuditAction;
import com.warehousemanagementsystem.model.EmployeeAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EmployeeAuditLogRepository extends JpaRepository<EmployeeAuditLog, UUID> {

    List<EmployeeAuditLog> findByTargetEmployeeIdOrderByOccurredAtDesc(Long targetEmployeeId);

    List<EmployeeAuditLog> findByActorEmployeeIdOrderByOccurredAtDesc(Long actorEmployeeId);

    List<EmployeeAuditLog> findByTargetEmployeeIdAndActionOrderByOccurredAtDesc(
            Long targetEmployeeId,
            EmployeeAuditAction action
    );
}