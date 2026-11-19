package com.warehousemanagementsystem.dto;

import com.warehousemanagementsystem.model.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class TransferResponseDto {
    private UUID transferId;
    private RegionalDistributionCenter sourceRdc;
    private Store sourceStore;
    private Store destinationStore;
    @Builder.Default private TransferStatus transferStatus = TransferStatus.PENDING;
    private OffsetDateTime shipDate;
    private OffsetDateTime estimatedArrival;
    private OffsetDateTime actualArrival;
    private List<InventoryTransferItem> transferItems;

    public static TransferResponseDto from (InventoryTransfer transfer) {
        return TransferResponseDto.builder()
            .transferId(transfer.getTransferId())
            .sourceRdc(transfer.getSourceRdc())
            .sourceStore(transfer.getSourceStore())
            .destinationStore(transfer.getDestinationStore())
            .transferStatus(transfer.getTransferStatus())
            .shipDate(transfer.getShipDate())
            .estimatedArrival(transfer.getEstimatedArrival())
            .actualArrival(transfer.getActualArrival())
            .transferItems(transfer.getTransferItems())
            .build();
    }
}