package com.warehousemanagementsystem.repository;

import com.warehousemanagementsystem.model.InventoryTransfer;
import com.warehousemanagementsystem.model.TransferStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InventoryTransferRepository extends JpaRepository<InventoryTransfer, UUID> {
    List<InventoryTransfer> findByDestinationStore_StoreId(UUID storeId);
    List<InventoryTransfer> findBySourceStore_StoreId(UUID storeId);
    List<InventoryTransfer> findBySourceRdc_RdcId(UUID rdcId);
    List<InventoryTransfer> findByTransferStatus(TransferStatus status);
}