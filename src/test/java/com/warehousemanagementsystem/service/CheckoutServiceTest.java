package com.warehousemanagementsystem.service;

import com.warehousemanagementsystem.exception.InsufficientStockException;
import com.warehousemanagementsystem.model.*;
import com.warehousemanagementsystem.repository.*;
import com.warehousemanagementsystem.security.EmployeeContextService;
import com.warehousemanagementsystem.dto.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckoutServiceTest {

    @Mock private SalesOrderRepository salesOrderRepository;
    @Mock private SalesOrderItemRepository salesOrderItemRepository;
    @Mock private StoreRepository storeRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private ProductRepository productRepository;
    @Mock private InventoryBalanceRepository inventoryBalanceRepository;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private EmployeeContextService employeeContextService;

    @InjectMocks
    private CheckoutService checkoutService;

    private UUID storeId;
    private UUID productId;
    private Store mockStore;
    private Product mockProduct;
    private Employee mockEmployee;

    @BeforeEach
    void setUp() {
        storeId = UUID.randomUUID();
        productId = UUID.randomUUID();

        mockStore = new Store();
        mockStore.setStoreId(storeId);

        mockProduct = new Product();
        mockProduct.setProductId(productId);
        mockProduct.setSku("TOOL-HAMMER");
        mockProduct.setProductName("Claw Hammer");

        mockEmployee = new Employee();
        mockEmployee.setEmployeeId(1L);
        mockEmployee.setStore(mockStore);
    }

    @Test
    void shouldThrowExceptionWhenStockIsInsufficient() {
        // Arrange
        CartItemDto cartItem = new CartItemDto(productId, 5, BigDecimal.valueOf(19.99));
        CheckoutRequestDto checkoutRequest = new CheckoutRequestDto(storeId, null, null, List.of(cartItem));

        when(employeeContextService.getCurrentEmployeeId()).thenReturn(1L);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(mockEmployee));
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(mockStore));
        when(productRepository.findById(productId)).thenReturn(Optional.of(mockProduct));

        InventoryBalance balance = new InventoryBalance();
        balance.setQuantityOnHand(2); // Only 2 available
        balance.setAllocatedQuantity(0);
        when(inventoryBalanceRepository.findById_StoreId(storeId)).thenReturn(List.of(balance));

        // Act & Assert
        assertThatThrownBy(() -> checkoutService.processCheckout(checkoutRequest))
                .isInstanceOf(InsufficientStockException.class);

        verify(salesOrderRepository, never()).save(any());
    }

    @Test
    void shouldProcessCheckoutSuccessfullyWhenStockIsSufficient() {
        // Arrange
        CartItemDto cartItem = new CartItemDto(productId, 2, BigDecimal.valueOf(19.99));
        CheckoutRequestDto checkoutRequest = new CheckoutRequestDto(storeId, null, null, List.of(cartItem));

        when(employeeContextService.getCurrentEmployeeId()).thenReturn(1L);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(mockEmployee));
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(mockStore));
        when(productRepository.findById(productId)).thenReturn(Optional.of(mockProduct));

        InventoryBalance balance = new InventoryBalance();
        balance.setQuantityOnHand(10);
        balance.setAllocatedQuantity(0);
        when(inventoryBalanceRepository.findById_StoreId(storeId)).thenReturn(List.of(balance));
        when(salesOrderRepository.save(any(SalesOrder.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        SalesOrder order = checkoutService.processCheckout(checkoutRequest);

        // Assert
        assertThat(order).isNotNull();
        assertThat(order.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(39.98));
        verify(inventoryBalanceRepository, times(1)).save(balance);
        assertThat(balance.getQuantityOnHand()).isEqualTo(8); // Deducted 2
    }
}