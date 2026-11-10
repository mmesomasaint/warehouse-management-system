package com.warehousemanagementsystem.repository;

import com.warehousemanagementsystem.model.StoreFulfillmentRouting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StoreFulfillmentRoutingRepository extends JpaRepository<StoreFulfillmentRouting, UUID> {
    List<StoreFulfillmentRouting> findByPrimaryRdc_RdcId(UUID rdcId);
    List<StoreFulfillmentRouting> findBySecondaryRdc_RdcId(UUID rdcId);
}