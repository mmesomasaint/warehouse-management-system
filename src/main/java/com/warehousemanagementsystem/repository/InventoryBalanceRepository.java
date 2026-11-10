package com.warehousemanagementsystem.repository;

import com.warehousemanagementsystem.model.InventoryBalance;
import com.warehousemanagementsystem.model.InventoryBalance.InventoryBalanceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InventoryBalanceRepository extends JpaRepository<InventoryBalance, InventoryBalanceId> {

    List<InventoryBalance> findById_StoreId(UUID storeId);

    List<InventoryBalance> findById_ProductId(UUID productId);

    List<InventoryBalance> findById_StoreIdAndId_ZoneId(UUID storeId, Long zoneId);

    // Alert queries where available stock is below safety threshold
    @Query("SELECT ib FROM InventoryBalance ib WHERE ib.id.storeId = :storeId AND (ib.quantityOnHand - ib.allocatedQuantity) <= ib.minimumSafetyStock")
    List<InventoryBalance> findLowStockAlertsByStore(@Param("storeId") UUID storeId);
}