package com.warehousemanagementsystem.repository;

import com.warehousemanagementsystem.model.TransferAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TransferAuditLogRepository extends JpaRepository<TransferAuditLog, UUID> {
    List<TransferAuditLog> findByTransferIdOrderByOccurredAtDesc(UUID transferId);
}