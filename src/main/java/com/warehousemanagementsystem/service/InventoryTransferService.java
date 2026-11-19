package com.warehousemanagementsystem.service;

import com.warehousemanagementsystem.dto.TransferResponseDto;
import com.warehousemanagementsystem.dto.CreateTransferRequestDto;
import com.warehousemanagementsystem.dto.ReceiveTransferRequestDto;
import com.warehousemanagementsystem.dto.RejectTransferRequestDto;
import com.warehousemanagementsystem.dto.TransferItemRequest;
import com.warehousemanagementsystem.model.*;
import com.warehousemanagementsystem.repository.*;
import com.warehousemanagementsystem.security.EmployeeContextService;
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
    private final InventoryZoneRepository inventoryZoneRepository;
    private final EmployeeContextService employeeContextService;

    public InventoryTransferService(InventoryTransferRepository transferRepository,
                                    InventoryTransferItemRepository transferItemRepository,
                                    RegionalDistributionCenterRepository rdcRepository,
                                    StoreRepository storeRepository,
                                    ProductRepository productRepository,
                                    InventoryBalanceRepository inventoryBalanceRepository,
                                    InventoryZoneRepository inventoryZoneRepository,
                                    EmployeeContextService employeeContextService) {
        this.transferRepository = transferRepository;
        this.transferItemRepository = transferItemRepository;
        this.rdcRepository = rdcRepository;
        this.storeRepository = storeRepository;
        this.productRepository = productRepository;
        this.inventoryBalanceRepository = inventoryBalanceRepository;
        this.inventoryZoneRepository = inventoryZoneRepository;
        this.employeeContextService = employeeContextService;
    }

    @Transactional
    public TransferResponseDto createTransfer(CreateTransferRequestDto createTransferRequest) {
        UUID sourceRdcId = createTransferRequest.sourceRdcId();
        UUID sourceStoreId = createTransferRequest.sourceStoreId();
        UUID destinationStoreId = createTransferRequest.destinationStoreId();
        List<TransferItemRequest> items = createTransferRequest.items();

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

        return buildTransferResponse(transfer);
    }

    @Transactional
    public TransferResponseDto editTransfer(UUID transferId, List<TransferItemRequest> newItems) {
        Long editorEmployeeId = employeeContextService.getCurrentEmployeeId();

        InventoryTransfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new EntityNotFoundException("Transfer not found: " + transferId));

        if (transfer.getTransferStatus() != TransferStatus.PENDING) {
            throw new IllegalStateException("Transfer can only be edited while in PENDING status.");
        }

        // Remove existing items and replace with updated manifest
        List<InventoryTransferItem> existingItems = transferItemRepository.findById_TransferId(transferId);
        transferItemRepository.deleteAll(existingItems);

        for (TransferItemRequest itemReq : newItems) {
            Product product = productRepository.findById(itemReq.productId())
                    .orElseThrow(() -> new EntityNotFoundException("Product not found: " + itemReq.productId()));

            InventoryTransferItem transferItem = new InventoryTransferItem();
            transferItem.setTransfer(transfer);
            transferItem.setProduct(product);
            transferItem.setQuantityShipped(itemReq.quantityShipped());
            transferItem.setQuantityReceived(0);

            transferItemRepository.save(transferItem);
        }

        // Optional REM: Log audit record tracking that editorEmployeeId modified this transfer
        return buildTransferResponse(transfer);
    }

    @Transactional
    public TransferResponseDto shipTransfer(UUID transferId) {
        InventoryTransfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new EntityNotFoundException("Transfer not found: " + transferId));

        if (transfer.getTransferStatus() != TransferStatus.PENDING) {
            throw new IllegalStateException("Transfer can only be shipped from PENDING status.");
        }

        transfer.setTransferStatus(TransferStatus.IN_TRANSIT);
        transfer.setShipDate(OffsetDateTime.now());

        return buildTransferResponse(transferRepository.save(transfer));
    }

    @Transactional
    public TransferResponseDto receiveTransfer(ReceiveTransferRequestDto receiveTransferRequest) {
        UUID transferId = receiveTransferRequest.transferId();
        Map<UUID, Integer> receivedQuantities = receiveTransferRequest.receivedQuantities();
        Long defaultZoneId = receiveTransferRequest.defaultZoneId();

        InventoryTransfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new EntityNotFoundException("Transfer not found: " + transferId));

        if (transfer.getTransferStatus() != TransferStatus.IN_TRANSIT && transfer.getTransferStatus() != TransferStatus.PICKED) {
            throw new IllegalStateException("Transfer must be in transit or picked to be received.");
        }

        List<InventoryTransferItem> items = transferItemRepository.findById_TransferId(transferId);
        Store destinationStore = transfer.getDestinationStore();
        UUID destStoreId = destinationStore.getStoreId();

        InventoryZone zone = inventoryZoneRepository.findById(defaultZoneId)
                .orElseThrow(() -> new EntityNotFoundException("Inventory zone not found: " + defaultZoneId));

        for (InventoryTransferItem item : items) {
            UUID productId = item.getProduct().getProductId();
            int receivedQty = receivedQuantities.getOrDefault(productId, item.getQuantityShipped());

            item.setQuantityReceived(receivedQty);
            transferItemRepository.save(item);

            InventoryBalance.InventoryBalanceId balanceId = new InventoryBalance.InventoryBalanceId(destStoreId, productId, defaultZoneId);
            InventoryBalance balance = inventoryBalanceRepository.findById(balanceId).orElseGet(() -> {
                InventoryBalance newBalance = new InventoryBalance();
                newBalance.setId(balanceId);
                newBalance.setStore(destinationStore);
                newBalance.setProduct(item.getProduct());
                newBalance.setZone(zone);
                newBalance.setQuantityOnHand(0);
                newBalance.setAllocatedQuantity(0);
                newBalance.setMinimumSafetyStock(10);
                return newBalance;
            });

            balance.setQuantityOnHand(balance.getQuantityOnHand() + receivedQty);
            inventoryBalanceRepository.save(balance);
        }

        transfer.setTransferStatus(TransferStatus.DELIVERED);
        transfer.setActualArrival(OffsetDateTime.now());

        return buildTransferResponse(transferRepository.save(transfer));
    }

    @Transactional
    public TransferResponseDto rejectTransfer(RejectTransferRequestDto rejectTransferRequest) {
        UUID transferId = rejectTransferRequest.transferId();
        String rejectionReason = rejectTransferRequest.rejectionReason();

        InventoryTransfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new EntityNotFoundException("Transfer not found: " + transferId));

        if (transfer.getTransferStatus() != TransferStatus.IN_TRANSIT && transfer.getTransferStatus() != TransferStatus.PENDING) {
            throw new IllegalStateException("Transfer cannot be rejected in its current status: " + transfer.getTransferStatus());
        }

        transfer.setTransferStatus(TransferStatus.REJECTED);
        // Optional REM: Save rejection reason to an audit log or notes column

        return buildTransferResponse(transferRepository.save(transfer));
    }

    private TransferResponseDto buildTransferResponse (InventoryTransfer transfer) {
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
