package com.warehousemanagementsystem.repository;

import com.warehousemanagementsystem.model.SalesOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SalesOrderItemRepository extends JpaRepository<SalesOrderItem, Long> {
    // Aligns with index: idx_sales_order_items_order (order_id)
    List<SalesOrderItem> findBySalesOrder_OrderId(Long orderId);

    List<SalesOrderItem> findByProduct_ProductId(UUID productId);
}