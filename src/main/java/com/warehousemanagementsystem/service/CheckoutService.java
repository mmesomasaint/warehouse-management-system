package com.warehousemanagementsystem.service;

import com.warehousemanagementsystem.model.*;
import com.warehousemanagementsystem.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
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
    public SalesOrder processCheckout(UUID storeId, Long customerId, Long employeeId, List<CartItemDto> cartItems) {
        // Fetch store and optional entities
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new EntityNotFoundException("Store not found: " + storeId));
        
        Customer customer = customerId != null ? customerRepository.findById(customerId).orElse(null) : null;
        Employee employee = employeeId != null ? employeeRepository.findById(employeeId).orElse(null) : null;

        // Initialize Sales Order header
        SalesOrder salesOrder = new SalesOrder();
        salesOrder.setStore(store);
        salesOrder.setCustomer(customer);
        salesOrder.setEmployee(employee);
        salesOrder.setOrderDate(OffsetDateTime.now());

        BigDecimal totalAmount = BigDecimal.ZERO;
        salesOrder = salesOrderRepository.save(salesOrder); // Save to get generated order ID

        // Process each line item, validate stock, and deduct inventory
        for (CartItemDto cartItem : cartItems) {
            Product product = productRepository.findById(cartItem.productId())
                    .orElseThrow(() -> new EntityNotFoundException("Product not found: " + cartItem.productId()));

            // Find stock in the store's inventory balances (e.g., searching across zones or a specific zone)
            List<InventoryBalance> balances = inventoryBalanceRepository.findById_StoreId(storeId);
            InventoryBalance balance = balances.stream()
                    .filter(b -> b.getProduct().getProductId().equals(cartItem.productId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Product " + product.getSku() + " not stocked in store " + storeId));

            int availableStock = balance.getQuantityOnHand() - balance.getAllocatedQuantity();
            if (availableStock < cartItem.quantity()) {
                throw new IllegalArgumentException("Insufficient stock for product: " + product.getProductName() 
                        + ". Requested: " + cartItem.quantity() + ", Available: " + availableStock);
            }

            // Deduct quantity on hand
            balance.setQuantityOnHand(balance.getQuantityOnHand() - cartItem.quantity());
            inventoryBalanceRepository.save(balance);

            // Create order line item
            SalesOrderItem orderItem = new SalesOrderItem();
            orderItem.setSalesOrder(salesOrder);
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.quantity());
            orderItem.setUnitPrice(cartItem.unitPrice());
            salesOrderItemRepository.save(orderItem);

            // Accumulate total
            BigDecimal lineTotal = cartItem.unitPrice().multiply(BigDecimal.valueOf(cartItem.quantity()));
            totalAmount = totalAmount.add(lineTotal);
        }

        // Finalize order total amount
        salesOrder.setTotalAmount(totalAmount);
        return salesOrderRepository.save(salesOrder);
    }

    // DTO record for cart items
    public record CartItemDto(UUID productId, int quantity, BigDecimal unitPrice) {}
}