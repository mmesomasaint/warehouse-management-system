package com.warehousemanagementsystem.repository;

import com.warehousemanagementsystem.model.InventoryTransferItem;
import com.warehousemanagementsystem.model.InventoryTransferItem.InventoryTransferItemId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InventoryTransferItemRepository extends JpaRepository<InventoryTransferItem, InventoryTransferItemId> {
    List<InventoryTransferItem> findById_TransferId(UUID transferId);
    List<InventoryTransferItem> findById_ProductId(UUID productId);
}