package com.warehousemanagementsystem.service;

import com.warehousemanagementsystem.dto.CreateTransferRequestDto;
import com.warehousemanagementsystem.dto.ReceiveTransferRequestDto;
import com.warehousemanagementsystem.dto.TransferItemRequest;
import com.warehousemanagementsystem.dto.TransferResponseDto;
import com.warehousemanagementsystem.event.TransferAuditedEvent;
import com.warehousemanagementsystem.model.*;
import com.warehousemanagementsystem.repository.*;
import com.warehousemanagementsystem.security.EmployeeContextService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.context.ApplicationEventPublisher;
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
    private final EmployeeRepository employeeRepository;
    private final EmployeeContextService employeeContextService;
    private final ApplicationEventPublisher eventPublisher;

    public InventoryTransferService(InventoryTransferRepository transferRepository,
                                    InventoryTransferItemRepository transferItemRepository,
                                    RegionalDistributionCenterRepository rdcRepository,
                                    StoreRepository storeRepository,
                                    ProductRepository productRepository,
                                    InventoryBalanceRepository inventoryBalanceRepository,
                                    InventoryZoneRepository inventoryZoneRepository,
                                    EmployeeRepository employeeRepository,
                                    EmployeeContextService employeeContextService,
                                    ApplicationEventPublisher eventPublisher) {
        this.transferRepository = transferRepository;
        this.transferItemRepository = transferItemRepository;
        this.rdcRepository = rdcRepository;
        this.storeRepository = storeRepository;
        this.productRepository = productRepository;
        this.inventoryBalanceRepository = inventoryBalanceRepository;
        this.inventoryZoneRepository = inventoryZoneRepository;
        this.employeeRepository = employeeRepository;
        this.employeeContextService = employeeContextService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public TransferResponseDto createTransfer(CreateTransferRequestDto request) {
        if (request.sourceRdcId() == null && request.sourceStoreId() == null) {
            throw new IllegalArgumentException("Either sourceRdcId or sourceStoreId must be provided.");
        }

        Long actorId = employeeContextService.getCurrentEmployeeId();
        OffsetDateTime now = OffsetDateTime.now();

        RegionalDistributionCenter sourceRdc = request.sourceRdcId() != null
                ? rdcRepository.findById(request.sourceRdcId()).orElse(null) : null;
        Store sourceStore = request.sourceStoreId() != null
                ? storeRepository.findById(request.sourceStoreId()).orElse(null) : null;
        Store destinationStore = storeRepository.findById(request.destinationStoreId())
                .orElseThrow(() -> new EntityNotFoundException("Destination store not found: " + request.destinationStoreId()));

        InventoryTransfer transfer = new InventoryTransfer();
        transfer.setSourceRdc(sourceRdc);
        transfer.setSourceStore(sourceStore);
        transfer.setDestinationStore(destinationStore);
        transfer.setTransferStatus(TransferStatus.PENDING);
        transfer.setCreatedByEmployeeId(actorId);
        transfer = transferRepository.save(transfer);

        for (TransferItemRequest itemReq : request.items()) {
            Product product = productRepository.findById(itemReq.productId())
                    .orElseThrow(() -> new EntityNotFoundException("Product not found: " + itemReq.productId()));

            InventoryTransferItem transferItem = new InventoryTransferItem();
            transferItem.setTransfer(transfer);
            transferItem.setProduct(product);
            transferItem.setQuantityShipped(itemReq.quantityShipped());
            transferItem.setQuantityReceived(0);
            transferItemRepository.save(transferItem);
        }

        eventPublisher.publishEvent(new TransferAuditedEvent(
                transfer.getTransferId(),
                TransferAuditAction.CREATED,
                actorId,
                null,
                null,
                "Created transfer with " + request.items().size() + " line item(s)",
                now
        ));

        return TransferResponseDto.from(transfer);
    }

    @Transactional
    public TransferResponseDto editTransfer(UUID transferId, List<TransferItemRequest> newItems) {
        Long editorId = employeeContextService.getCurrentEmployeeId();
        OffsetDateTime now = OffsetDateTime.now();

        InventoryTransfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new EntityNotFoundException("Transfer not found: " + transferId));

        if (transfer.getTransferStatus() != TransferStatus.PENDING) {
            throw new IllegalStateException("Transfer can only be edited while in PENDING status.");
        }

        EditorType editorType = resolveEditorType(transfer, editorId);

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

        transfer.setLastEditedByEmployeeId(editorId);
        transfer.setLastEditedAt(now);
        transfer = transferRepository.save(transfer);

        eventPublisher.publishEvent(new TransferAuditedEvent(
                transfer.getTransferId(),
                TransferAuditAction.EDITED,
                editorId,
                editorType,
                null,
                "Edited by " + editorType + " (" + newItems.size() + " line item(s))",
                now
        ));

        return TransferResponseDto.from(transfer);
    }

    @Transactional
    public TransferResponseDto shipTransfer(UUID transferId) {
        Long actorId = employeeContextService.getCurrentEmployeeId();
        OffsetDateTime now = OffsetDateTime.now();

        InventoryTransfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new EntityNotFoundException("Transfer not found: " + transferId));

        if (transfer.getTransferStatus() != TransferStatus.PENDING) {
            throw new IllegalStateException("Transfer can only be shipped from PENDING status.");
        }

        transfer.setTransferStatus(TransferStatus.IN_TRANSIT);
        transfer.setShipDate(now);
        transfer = transferRepository.save(transfer);

        eventPublisher.publishEvent(new TransferAuditedEvent(
                transfer.getTransferId(),
                TransferAuditAction.SHIPPED,
                actorId,
                null,
                null,
                "Shipped transfer to store " + transfer.getDestinationStore().getStoreId(),
                now
        ));

        return TransferResponseDto.from(transfer);
    }

    @Transactional
    public TransferResponseDto receiveTransfer(ReceiveTransferRequestDto request) {
        Long actorId = employeeContextService.getCurrentEmployeeId();
        OffsetDateTime now = OffsetDateTime.now();

        InventoryTransfer transfer = transferRepository.findById(request.transferId())
                .orElseThrow(() -> new EntityNotFoundException("Transfer not found: " + request.transferId()));

        if (transfer.getTransferStatus() != TransferStatus.IN_TRANSIT
                && transfer.getTransferStatus() != TransferStatus.PICKED) {
            throw new IllegalStateException("Transfer must be in transit or picked to be received.");
        }

        List<InventoryTransferItem> items = transferItemRepository.findById_TransferId(request.transferId());
        Store destinationStore = transfer.getDestinationStore();
        UUID destStoreId = destinationStore.getStoreId();

        InventoryZone zone = inventoryZoneRepository.findById(request.zoneId())
                .orElseThrow(() -> new EntityNotFoundException("Inventory zone not found: " + request.zoneId()));

        Map<UUID, Integer> receivedMap = request.receivedQuantities();
        for (InventoryTransferItem item : items) {
            UUID productId = item.getProduct().getProductId();
            int receivedQty = receivedMap.getOrDefault(productId, item.getQuantityShipped());

            item.setQuantityReceived(receivedQty);
            transferItemRepository.save(item);

            InventoryBalance.InventoryBalanceId balanceId =
                    new InventoryBalance.InventoryBalanceId(destStoreId, productId, request.zoneId());
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
        transfer.setActualArrival(now);
        transfer = transferRepository.save(transfer);

        eventPublisher.publishEvent(new TransferAuditedEvent(
                transfer.getTransferId(),
                TransferAuditAction.RECEIVED,
                actorId,
                null,
                null,
                "Received transfer into zone " + zone.getZoneId(),
                now
        ));

        return TransferResponseDto.from(transfer);
    }

    @Transactional
    public TransferResponseDto rejectTransfer(UUID transferId, String rejectionReason) {
        if (rejectionReason == null || rejectionReason.isBlank()) {
            throw new IllegalArgumentException("Rejection reason is required.");
        }

        Long actorId = employeeContextService.getCurrentEmployeeId();
        OffsetDateTime now = OffsetDateTime.now();

        InventoryTransfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new EntityNotFoundException("Transfer not found: " + transferId));

        if (transfer.getTransferStatus() != TransferStatus.IN_TRANSIT
                && transfer.getTransferStatus() != TransferStatus.PENDING) {
            throw new IllegalStateException("Transfer cannot be rejected in its current status: " + transfer.getTransferStatus());
        }

        transfer.setTransferStatus(TransferStatus.REJECTED);
        transfer.setRejectionReason(rejectionReason);
        transfer = transferRepository.save(transfer);

        eventPublisher.publishEvent(new TransferAuditedEvent(
                transfer.getTransferId(),
                TransferAuditAction.REJECTED,
                actorId,
                null,
                rejectionReason,
                "Rejected transfer with reason: " + rejectionReason,
                now
        ));

        return TransferResponseDto.from(transfer);
    }

    private EditorType resolveEditorType(InventoryTransfer transfer, Long editorId) {
        if (editorId != null && editorId.equals(transfer.getCreatedByEmployeeId())) {
            return EditorType.CREATOR;
        }
        if (editorId != null && transfer.getDestinationStore() != null) {
            UUID destStoreId = transfer.getDestinationStore().getStoreId();
            boolean belongsToDestStore = employeeRepository.findById(editorId)
                    .map(emp -> destStoreId.equals(emp.getStoreId()))
                    .orElse(false);
            if (belongsToDestStore) {
                return EditorType.RECEIVER;
            }
        }
        return EditorType.OTHER;
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
