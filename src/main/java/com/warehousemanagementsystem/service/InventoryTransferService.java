package com.warehousemanagementsystem.service;

import com.warehousemanagementsystem.model.*;
import com.warehousemanagementsystem.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class InventoryTransferService {

    private final InventoryTransferRepository transferRepository;
    private final InventoryTransferItemRepository transferItemRepository;
    private final RegionalDistributionCenterRepository rdcRepository;
    private final StoreRepository storeRepository;
    private final ProductRepository productRepository;
    private final InventoryBalanceRepository inventoryBalanceRepository;

    public InventoryTransferService(InventoryTransferRepository transferRepository,
                                    InventoryTransferItemRepository transferItemRepository,
                                    RegionalDistributionCenterRepository rdcRepository,
                                    StoreRepository storeRepository,
                                    ProductRepository productRepository,
                                    InventoryBalanceRepository inventoryBalanceRepository) {
        this.transferRepository = transferRepository;
        this.transferItemRepository = transferItemRepository;
        this.rdcRepository = rdcRepository;
        this.storeRepository = storeRepository;
        this.productRepository = productRepository;
        this.inventoryBalanceRepository = inventoryBalanceRepository;
    }

    @Transactional
    public InventoryTransfer createTransfer(UUID sourceRdcId, UUID sourceStoreId, UUID destinationStoreId, List<TransferItemRequest> items) {
        if (sourceRdcId == null && sourceStoreId == null) {
            throw new IllegalArgumentException("Either sourceRdcId or sourceStoreId must be provided.");
        }

        RegionalDistributionCenter sourceRdc = sourceRdcId != null ? rdcRepository.findById(sourceRdcId).orElse(null) : null;
        Store sourceStore = sourceStoreId != null ? storeRepository.findById(sourceStoreId).orElse(null) : null;
        Store destinationStore = storeRepository.findById(destinationStoreId)
                .orElseThrow(() -> new EntityNotFoundException("Destination store not found: " + destinationStoreId));

        InventoryTransfer transfer = new InventoryTransfer();
        transfer.setSourceRdc(sourceRdc);
        transfer.setSourceStore(sourceStore);
        transfer.setDestinationStore(destinationStore);
        transfer.setTransferStatus(TransferStatus.PENDING);

        transfer = transferRepository.save(transfer);

        for (TransferItemRequest itemReq : items) {
            Product product = productRepository.findById(itemReq.productId())
                    .orElseThrow(() -> new EntityNotFoundException("Product not found: " + itemReq.productId()));

            InventoryTransferItem transferItem = new InventoryTransferItem();
            transferItem.setTransfer(transfer);
            transferItem.setProduct(product);
            transferItem.setQuantityShipped(itemReq.quantityShipped());
            transferItem.setQuantityReceived(0);

            transferItemRepository.save(transferItem);
        }

        return transfer;
    }

    @Transactional
    public InventoryTransfer shipTransfer(UUID transferId) {
        InventoryTransfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new EntityNotFoundException("Transfer not found: " + transferId));

        if (transfer.getTransferStatus() != TransferStatus.PENDING) {
            throw new IllegalStateException("Transfer can only be shipped from PENDING status.");
        }

        transfer.setTransferStatus(TransferStatus.IN_TRANSIT);
        transfer.setShipDate(OffsetDateTime.now());
        return transferRepository.save(transfer);
    }

    @Transactional
    public InventoryTransfer receiveTransfer(UUID transferId, Map<UUID, Integer> receivedQuantities, Long defaultZoneId) {
        InventoryTransfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new EntityNotFoundException("Transfer not found: " + transferId));

        if (transfer.getTransferStatus() != TransferStatus.IN_TRANSIT && transfer.getTransferStatus() != TransferStatus.PICKED) {
            throw new IllegalStateException("Transfer must be in transit or picked to be received.");
        }

        List<InventoryTransferItem> items = transferItemRepository.findById_TransferId(transferId);
        UUID destStoreId = transfer.getDestinationStore().getStoreId();

        for (InventoryTransferItem item : items) {
            UUID productId = item.getProduct().getProductId();
            int receivedQty = receivedQuantities.getOrDefault(productId, item.getQuantityShipped());
            
            item.setQuantityReceived(receivedQty);
            transferItemRepository.save(item);

            // Update or create destination store inventory balance
            InventoryBalance.InventoryBalanceId balanceId = new InventoryBalance.InventoryBalanceId(destStoreId, productId, defaultZoneId);
            InventoryBalance balance = inventoryBalanceRepository.findById(balanceId).orElseGet(() -> {
                InventoryBalance newBalance = new InventoryBalance();
                // set up embedded id and relationships manually if needed
                return newBalance;
            });
            
            // If it's a new balance object from getOrElse
            if (balance.getId() == null) {
                // You would set fields here or fetch via proper mapping
            }
            
            balance.setQuantityOnHand(balance.getQuantityOnHand() + receivedQty);
            inventoryBalanceRepository.save(balance);
        }

        transfer.setTransferStatus(TransferStatus.DELIVERED);
        transfer.setActualArrival(OffsetDateTime.now());
        return transferRepository.save(transfer);
    }

    public record TransferItemRequest(UUID productId, int quantityShipped) {}
}