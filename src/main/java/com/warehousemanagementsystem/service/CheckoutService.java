package com.warehousemanagementsystem.service;

import com.warehousemanagementsystem.model.*;
import com.warehousemanagementsystem.repository.*;
import com.warehousemanagementsystem.dto.CartItemDto;
import com.warehousemanagementsystem.dto.CheckoutRequestDto;
import com.warehousemanagementsystem.dto.CheckoutResponseDto;
import com.warehousemanagementsystem.exception.InsufficientStockException;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class CheckoutService {

    private final SalesOrderRepository salesOrderRepository;
    private final SalesOrderItemRepository salesOrderItemRepository;
    private final StoreRepository storeRepository;
    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;
    private final ProductRepository productRepository;
    private final InventoryBalanceRepository inventoryBalanceRepository;

    public CheckoutService(SalesOrderRepository salesOrderRepository,
                        SalesOrderItemRepository salesOrderItemRepository,
                        StoreRepository storeRepository,
                        CustomerRepository customerRepository,
                        EmployeeRepository employeeRepository,
                        ProductRepository productRepository,
                        InventoryBalanceRepository inventoryBalanceRepository) {
        this.salesOrderRepository = salesOrderRepository;
        this.salesOrderItemRepository = salesOrderItemRepository;
        this.storeRepository = storeRepository;
        this.customerRepository = customerRepository;
        this.employeeRepository = employeeRepository;
        this.productRepository = productRepository;
        this.inventoryBalanceRepository = inventoryBalanceRepository;
    }

    @Transactional
    @PreAuthorize("hasAuthority('sales:checkout')")
    public CheckoutResponseDto processCheckout(CheckoutRequestDto request) {
        // Initialize variables
        UUID storeId = request.storeId();
        Long customerId = request.customerId();
        Long employeeId = request.employeeId();
        List<CartItemDto> cartItems = request.cartItems();

        // Fetch store and optional entities
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new EntityNotFoundException("Store not found: " + storeId));
        
        Customer customer = customerId != null ? customerRepository.findById(customerId).orElse(null) : null;
        Employee employee = employeeId != null ? employeeRepository.findById(employeeId).orElse(null) : null;

        // Validate stock and calculate totals BEFORE saving the SalesOrder
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<SalesOrderItem> itemsToSave = new ArrayList<>();
        List<InventoryBalance> balancesToUpdate = new ArrayList<>();

        for (CartItemDto cartItem : cartItems) {
            Product product = productRepository.findById(cartItem.productId())
                    .orElseThrow(() -> new EntityNotFoundException("Product not found: " + cartItem.productId()));

            List<InventoryBalance> balances = inventoryBalanceRepository.findById_StoreId(storeId);
            InventoryBalance balance = balances.stream()
                    .filter(b -> b.getProduct() != null && b.getProduct().getProductId().equals(cartItem.productId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Product " + product.getSku() + " not stocked in store " + storeId));

            int availableStock = balance.getQuantityOnHand() - balance.getAllocatedQuantity();
            if (availableStock < cartItem.quantity()) {
                throw new InsufficientStockException(product.getProductId(), cartItem.quantity(), availableStock);
            }

            balance.setQuantityOnHand(balance.getQuantityOnHand() - cartItem.quantity());
            balancesToUpdate.add(balance);

            SalesOrderItem orderItem = new SalesOrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.quantity());
            orderItem.setUnitPrice(cartItem.unitPrice());
            itemsToSave.add(orderItem);

            totalAmount = totalAmount.add(cartItem.unitPrice().multiply(BigDecimal.valueOf(cartItem.quantity())));
        }

        // Now persist everything cleanly once all checks pass
        SalesOrder salesOrder = new SalesOrder();
        salesOrder.setStore(store);
        salesOrder.setCustomer(customer);
        salesOrder.setEmployee(employee);
        salesOrder.setOrderDate(OffsetDateTime.now());
        salesOrder.setTotalAmount(totalAmount);
        salesOrder = salesOrderRepository.save(salesOrder);

        for (SalesOrderItem item : itemsToSave) {
            item.setSalesOrder(salesOrder);
            salesOrderItemRepository.save(item);
        }
        inventoryBalanceRepository.saveAll(balancesToUpdate);


        // Convert to Dto response
        return CheckoutResponseDto.builder()
            .orderId(salesOrder.getOrderId())
            .store(salesOrder.getStore())
            .customer(salesOrder.getCustomer())
            .employee(salesOrder.getEmployee()) 
            .orderDate(salesOrder.getOrderDate()) 
            .totalAmount(salesOrder.getTotalAmount()) 
            .orderItems(salesOrder.getOrderItems())
            .build();
    }
}