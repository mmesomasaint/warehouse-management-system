package com.warehousemanagementsystem.repository;

import com.warehousemanagementsystem.model.RdcInventoryBalance;
import com.warehousemanagementsystem.model.RdcInventoryBalance.RdcInventoryBalanceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RdcInventoryBalanceRepository extends JpaRepository<RdcInventoryBalance, RdcInventoryBalanceId> {
    List<RdcInventoryBalance> findById_RdcId(UUID rdcId);
    List<RdcInventoryBalance> findById_ProductId(UUID productId);
}