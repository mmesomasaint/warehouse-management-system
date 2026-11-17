package com.warehousemanagementsystem.exception;

import com.warehousemanagementsystem.model.TransferStatus;
import java.util.UUID;

public class InvalidTransferStateException extends WarehouseDomainException {
    private final UUID transferId;
    private final TransferStatus currentStatus;

    public InvalidTransferStateException(UUID transferId, TransferStatus currentStatus, String message) {
        super(String.format("Transfer [%s] in status [%s] cannot perform this action: %s", transferId, currentStatus, message));
        this.transferId = transferId;
        this.currentStatus = currentStatus;
    }

    public UUID getTransferId() { return transferId; }
    public TransferStatus getCurrentStatus() { return currentStatus; }
}