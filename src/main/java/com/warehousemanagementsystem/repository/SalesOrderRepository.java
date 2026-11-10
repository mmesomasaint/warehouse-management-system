package com.warehousemanagementsystem.repository;

import com.warehousemanagementsystem.model.SalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {
    // Aligns with index: idx_sales_orders_store_date (store_id, order_date DESC)
    List<SalesOrder> findByStore_StoreIdOrderByOrderDateDesc(UUID storeId);

    List<SalesOrder> findByCustomer_CustomerId(Long customerId);

    List<SalesOrder> findByEmployee_EmployeeId(Long employeeId);

    List<SalesOrder> findByStore_StoreIdAndOrderDateBetween(UUID storeId, OffsetDateTime startDate, OffsetDateTime endDate);
}