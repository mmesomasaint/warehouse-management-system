package com.warehousemanagementsystem.service;

import com.warehousemanagementsystem.model.*;
import com.warehousemanagementsystem.repository.*;
import com.warehousemanagementsystem.security.EmployeeContextService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryTransferServiceTest {

    @Mock private InventoryTransferRepository transferRepository;
    @Mock private InventoryTransferItemRepository transferItemRepository;
    @Mock private RegionalDistributionCenterRepository rdcRepository;
    @Mock private StoreRepository storeRepository;
    @Mock private ProductRepository productRepository;
    @Mock private InventoryBalanceRepository inventoryBalanceRepository;
    @Mock private InventoryZoneRepository inventoryZoneRepository;
    @Mock private EmployeeContextService employeeContextService;

    @InjectMocks
    private InventoryTransferService transferService;

    private UUID rdcId;
    private UUID storeId;
    private UUID productId;
    private UUID transferId;
    private Long zoneId;

    private RegionalDistributionCenter mockRdc;
    private Store mockStore;
    private Product mockProduct;
    private InventoryZone mockZone;
    private Employee mockEmployee;

    @BeforeEach
    void setUp() {
        rdcId = UUID.randomUUID();
        storeId = UUID.randomUUID();
        productId = UUID.randomUUID();
        transferId = UUID.randomUUID();
        zoneId = 1L;

        mockRdc = new RegionalDistributionCenter();
        mockRdc.setRdcId(rdcId);

        mockStore = new Store();
        mockStore.setStoreId(storeId);

        mockProduct = new Product();
        mockProduct.setProductId(productId);
        mockProduct.setSku("TOOL-DRILL");

        mockZone = new InventoryZone();
        mockZone.setZoneId(zoneId);
        mockZone.setZoneName("Back Stockroom");

        mockEmployee = new Employee();
        mockEmployee.setEmployeeId(1L);
        mockEmployee.setStore(mockStore);
    }

    @Test
    void shouldThrowExceptionWhenBothSourcesAreNull() {
        // Arrange
        List<InventoryTransferService.TransferItemRequest> items = List.of(
                new InventoryTransferService.TransferItemRequest(productId, 10)
        );

        // Act & Assert
        assertThatThrownBy(() -> transferService.createTransfer(null, null, storeId, items))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Either sourceRdcId or sourceStoreId must be provided");

        verify(transferRepository, never()).save(any());
    }

    @Test
    void shouldCreateTransferSuccessfully() {
        // Arrange
        List<InventoryTransferService.TransferItemRequest> items = List.of(
                new InventoryTransferService.TransferItemRequest(productId, 15)
        );

        when(rdcRepository.findById(rdcId)).thenReturn(Optional.of(mockRdc));
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(mockStore));
        when(productRepository.findById(productId)).thenReturn(Optional.of(mockProduct));
        when(transferRepository.save(any(InventoryTransfer.class))).thenAnswer(inv -> {
            InventoryTransfer t = inv.getArgument(0);
            t.setTransferId(transferId);
            return t;
        });

        // Act
        InventoryTransfer transfer = transferService.createTransfer(rdcId, null, storeId, items);

        // Assert
        assertThat(transfer).isNotNull();
        assertThat(transfer.getTransferStatus()).isEqualTo(TransferStatus.PENDING);
        assertThat(transfer.getSourceRdc()).isEqualTo(mockRdc);
        assertThat(transfer.getDestinationStore()).isEqualTo(mockStore);
        verify(transferItemRepository, times(1)).save(any(InventoryTransferItem.class));
    }

    @Test
    void shouldShipTransferSuccessfully() {
        // Arrange
        when(employeeContextService.getCurrentEmployee()).thenReturn(mockEmployee);

        InventoryTransfer pendingTransfer = new InventoryTransfer();
        pendingTransfer.setTransferId(transferId);
        pendingTransfer.setTransferStatus(TransferStatus.PENDING);

        when(transferRepository.findById(transferId)).thenReturn(Optional.of(pendingTransfer));
        when(transferRepository.save(any(InventoryTransfer.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        InventoryTransfer shippedTransfer = transferService.shipTransfer(transferId);

        // Assert
        assertThat(shippedTransfer.getTransferStatus()).isEqualTo(TransferStatus.IN_TRANSIT);
        assertThat(shippedTransfer.getShipDate()).isNotNull();
        verify(transferRepository, times(1)).save(pendingTransfer);
    }

    @Test
    void shouldThrowExceptionWhenShippingNonPendingTransfer() {
        // Arrange
        when(employeeContextService.getCurrentEmployee()).thenReturn(mockEmployee);

        InventoryTransfer transitTransfer = new InventoryTransfer();
        transitTransfer.setTransferId(transferId);
        transitTransfer.setTransferStatus(TransferStatus.IN_TRANSIT);

        when(transferRepository.findById(transferId)).thenReturn(Optional.of(transitTransfer));

        // Act & Assert
        assertThatThrownBy(() -> transferService.shipTransfer(transferId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Transfer can only be shipped from PENDING status");

        verify(transferRepository, never()).save(any());
    }

    @Test
    void shouldReceiveTransferAndCreateNewInventoryBalanceSuccessfully() {
        // Arrange
        when(employeeContextService.getCurrentEmployee()).thenReturn(mockEmployee);

        InventoryTransfer transitTransfer = new InventoryTransfer();
        transitTransfer.setTransferId(transferId);
        transitTransfer.setTransferStatus(TransferStatus.IN_TRANSIT);
        transitTransfer.setDestinationStore(mockStore);

        InventoryTransferItem transferItem = new InventoryTransferItem();
        transferItem.setProduct(mockProduct);
        transferItem.setQuantityShipped(20);

        Map<UUID, InventoryTransferService.TransferReceiptDto> receiptMap = Map.of(
                productId, new InventoryTransferService.TransferReceiptDto(20, 0)
        );

        when(transferRepository.findById(transferId)).thenReturn(Optional.of(transitTransfer));
        when(transferItemRepository.findById_TransferId(transferId)).thenReturn(List.of(transferItem));
        when(inventoryZoneRepository.findById(zoneId)).thenReturn(Optional.of(mockZone));
        
        // Simulate inventory balance does not exist yet for this store/product/zone
        when(inventoryBalanceRepository.findById(any())).thenReturn(Optional.empty());
        when(transferRepository.save(any(InventoryTransfer.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        InventoryTransfer deliveredTransfer = transferService.receiveOrRejectTransfer(transferId, receiptMap, zoneId);

        // Assert
        assertThat(deliveredTransfer.getTransferStatus()).isEqualTo(TransferStatus.DELIVERED);
        assertThat(deliveredTransfer.getActualArrival()).isNotNull();
        verify(inventoryBalanceRepository, times(1)).save(any(InventoryBalance.class));
    }
}