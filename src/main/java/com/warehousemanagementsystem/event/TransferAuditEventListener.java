package com.warehousemanagementsystem.event;

import com.warehousemanagementsystem.model.TransferAuditLog;
import com.warehousemanagementsystem.repository.TransferAuditLogRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class TransferAuditEventListener {

    private final TransferAuditLogRepository auditLogRepository;

    public TransferAuditEventListener(TransferAuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onTransferAudited(TransferAuditedEvent event) {
        TransferAuditLog log = new TransferAuditLog();
        log.setTransferId(event.transferId());
        log.setAction(event.action());
        log.setActorEmployeeId(event.actorEmployeeId());
        log.setEditorType(event.editorType());
        log.setReason(event.reason());
        log.setDetails(event.details());
        log.setOccurredAt(event.occurredAt());
        auditLogRepository.save(log);
    }
}