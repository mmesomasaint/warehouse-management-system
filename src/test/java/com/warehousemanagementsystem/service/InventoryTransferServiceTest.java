package com.warehousemanagementsystem.service;

import com.warehousemanagementsystem.event.TransferAuditedEvent;
import com.warehousemanagementsystem.dto.CreateTransferRequestDto;
import com.warehousemanagementsystem.dto.EditTransferRequestDto;
import com.warehousemanagementsystem.dto.ReceiveTransferRequestDto;
import com.warehousemanagementsystem.dto.RejectTransferRequestDto;
import com.warehousemanagementsystem.dto.TransferItemRequest;
import com.warehousemanagementsystem.dto.TransferResponseDto;
import com.warehousemanagementsystem.model.*;
import com.warehousemanagementsystem.repository.*;
import com.warehousemanagementsystem.security.EmployeeContextService;
import org.springframework.context.ApplicationEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    @Mock private ApplicationEventPublisher eventPublisher;

    @Mock private InventoryTransferRepository transferRepository;
    @Mock private InventoryTransferItemRepository transferItemRepository;
    @Mock private RegionalDistributionCenterRepository rdcRepository;
    @Mock private StoreRepository storeRepository;
    @Mock private ProductRepository productRepository;
    @Mock private InventoryBalanceRepository inventoryBalanceRepository;
    @Mock private InventoryZoneRepository inventoryZoneRepository;
    @Mock private EmployeeRepository employeeRepository;
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
        List<TransferItemRequest> items = List.of(
                new TransferItemRequest(productId, 10)
        );
        CreateTransferRequestDto createTransferRequest = new CreateTransferRequestDto(null, null, storeId, items);

        assertThatThrownBy(() -> transferService.createTransfer(createTransferRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Either sourceRdcId or sourceStoreId must be provided");

        verify(transferRepository, never()).save(any());
    }

    @Test
    void shouldCreateTransferSuccessfully() {
        List<TransferItemRequest> items = List.of(
                new TransferItemRequest(productId, 15)
        );
        CreateTransferRequestDto createTransferRequest = new CreateTransferRequestDto(rdcId, null, storeId, items);

        when(rdcRepository.findById(rdcId)).thenReturn(Optional.of(mockRdc));
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(mockStore));
        when(productRepository.findById(productId)).thenReturn(Optional.of(mockProduct));
        when(transferRepository.save(any(InventoryTransfer.class))).thenAnswer(inv -> {
            InventoryTransfer t = inv.getArgument(0);
            t.setTransferId(transferId);
            return t;
        });

        TransferResponseDto transferResponse = transferService.createTransfer(createTransferRequest);

        assertThat(transferResponse).isNotNull();
        assertThat(transferResponse.getTransferStatus()).isEqualTo(TransferStatus.PENDING);
        assertThat(transferResponse.getSourceRdc()).isEqualTo(mockRdc);
        assertThat(transferResponse.getDestinationStore()).isEqualTo(mockStore);
        verify(transferItemRepository, times(1)).save(any(InventoryTransferItem.class));
        verify(eventPublisher).publishEvent(any(TransferAuditedEvent.class));
    }

    @Test
    void shouldEditPendingTransferItemsSuccessfully() {
        List<TransferItemRequest> updatedItems = List.of(
                new TransferItemRequest(productId, 25)
        );

        InventoryTransfer pendingTransfer = new InventoryTransfer();
        pendingTransfer.setTransferId(transferId);
        pendingTransfer.setTransferStatus(TransferStatus.PENDING);
        pendingTransfer.setDestinationStore(mockStore);

        InventoryTransferItem existingItem = new InventoryTransferItem();
        existingItem.setProduct(mockProduct);
        existingItem.setQuantityShipped(10);

        EditTransferRequestDto editTransferRequest = new EditTransferRequestDto(transferId, updatedItems);

        when(employeeContextService.getCurrentEmployeeId()).thenReturn(1L);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(mockEmployee));
        when(transferRepository.findById(transferId)).thenReturn(Optional.of(pendingTransfer));
        when(transferItemRepository.findById_TransferId(transferId)).thenReturn(List.of(existingItem));
        when(productRepository.findById(productId)).thenReturn(Optional.of(mockProduct));
        when(transferRepository.save(any(InventoryTransfer.class))).thenAnswer(inv -> inv.getArgument(0));

        TransferResponseDto response = transferService.editTransfer(editTransferRequest);

        assertThat(response).isNotNull();
        assertThat(response.getTransferStatus()).isEqualTo(TransferStatus.PENDING);
        verify(transferItemRepository).deleteAll(List.of(existingItem));
        verify(transferItemRepository, times(1)).save(any(InventoryTransferItem.class));
        verify(transferRepository, times(1)).save(pendingTransfer);
        verify(eventPublisher).publishEvent(any(TransferAuditedEvent.class));
    }

    @Test
    void shouldRejectEditingNonPendingTransfer() {
        List<TransferItemRequest> updatedItems = List.of(
                new TransferItemRequest(productId, 25)
        );

        EditTransferRequestDto editTransferRequest = new EditTransferRequestDto(transferId, updatedItems);

        InventoryTransfer shippedTransfer = new InventoryTransfer();
        shippedTransfer.setTransferId(transferId);
        shippedTransfer.setTransferStatus(TransferStatus.IN_TRANSIT);

        when(employeeContextService.getCurrentEmployeeId()).thenReturn(1L);
        when(transferRepository.findById(transferId)).thenReturn(Optional.of(shippedTransfer));

        assertThatThrownBy(() -> transferService.editTransfer(editTransferRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("PENDING");

        verify(transferItemRepository, never()).save(any());
    }

    @Test
    void shouldShipTransferSuccessfully() {
        InventoryTransfer pendingTransfer = new InventoryTransfer();
        pendingTransfer.setTransferId(transferId);
        pendingTransfer.setTransferStatus(TransferStatus.PENDING);
        pendingTransfer.setDestinationStore(mockStore);

        when(transferRepository.findById(transferId)).thenReturn(Optional.of(pendingTransfer));
        when(transferRepository.save(any(InventoryTransfer.class))).thenAnswer(inv -> inv.getArgument(0));

        TransferResponseDto shippedTransferResponse = transferService.shipTransfer(transferId);

        assertThat(shippedTransferResponse.getTransferStatus()).isEqualTo(TransferStatus.IN_TRANSIT);
        assertThat(shippedTransferResponse.getShipDate()).isNotNull();
        verify(transferRepository, times(1)).save(pendingTransfer);
    }

    @Test
    void shouldThrowExceptionWhenShippingNonPendingTransfer() {
        InventoryTransfer transitTransfer = new InventoryTransfer();
        transitTransfer.setTransferId(transferId);
        transitTransfer.setTransferStatus(TransferStatus.IN_TRANSIT);

        when(transferRepository.findById(transferId)).thenReturn(Optional.of(transitTransfer));

        assertThatThrownBy(() -> transferService.shipTransfer(transferId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Transfer can only be shipped from PENDING status");

        verify(transferRepository, never()).save(any());
    }

    @Test
    void shouldReceiveTransferAndCreateNewInventoryBalanceSuccessfully() {
        InventoryTransfer transitTransfer = new InventoryTransfer();
        transitTransfer.setTransferId(transferId);
        transitTransfer.setTransferStatus(TransferStatus.IN_TRANSIT);
        transitTransfer.setDestinationStore(mockStore);

        InventoryTransferItem transferItem = new InventoryTransferItem();
        transferItem.setProduct(mockProduct);
        transferItem.setQuantityShipped(20);

        Map<UUID, Integer> receivedQty = Map.of(productId, 20);
        ReceiveTransferRequestDto receiveTransferRequest = new ReceiveTransferRequestDto(transferId, receivedQty, zoneId);

        when(transferRepository.findById(transferId)).thenReturn(Optional.of(transitTransfer));
        when(transferItemRepository.findById_TransferId(transferId)).thenReturn(List.of(transferItem));
        when(inventoryZoneRepository.findById(zoneId)).thenReturn(Optional.of(mockZone));
        when(inventoryBalanceRepository.findById(any())).thenReturn(Optional.empty());
        when(transferRepository.save(any(InventoryTransfer.class))).thenAnswer(inv -> inv.getArgument(0));

        TransferResponseDto deliveredTransferResponse = transferService.receiveTransfer(receiveTransferRequest);

        assertThat(deliveredTransferResponse.getTransferStatus()).isEqualTo(TransferStatus.DELIVERED);
        assertThat(deliveredTransferResponse.getActualArrival()).isNotNull();
        verify(inventoryBalanceRepository, times(1)).save(any(InventoryBalance.class));
        verify(eventPublisher).publishEvent(any(TransferAuditedEvent.class));
    }

    @Test
    void shouldRejectInTransitTransferWithReason() {
        InventoryTransfer transitTransfer = new InventoryTransfer();
        transitTransfer.setTransferId(transferId);
        transitTransfer.setTransferStatus(TransferStatus.IN_TRANSIT);
        transitTransfer.setDestinationStore(mockStore);

        when(employeeContextService.getCurrentEmployeeId()).thenReturn(1L);
        when(transferRepository.findById(transferId)).thenReturn(Optional.of(transitTransfer));
        when(transferRepository.save(any(InventoryTransfer.class))).thenAnswer(inv -> inv.getArgument(0));

        RejectTransferRequestDto rejectTransferRequest = new RejectTransferRequestDto(transferId, "Pallet water damage on arrival");
        TransferResponseDto rejectedResponse = transferService.rejectTransfer(rejectTransferRequest);

        assertThat(rejectedResponse.getTransferStatus()).isEqualTo(TransferStatus.REJECTED);
        verify(transferRepository, times(1)).save(transitTransfer);
        verify(inventoryBalanceRepository, never()).save(any());
        verify(eventPublisher).publishEvent(any(TransferAuditedEvent.class));
    }
}