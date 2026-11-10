package com.warehousemanagementsystem.repository;

import com.warehousemanagementsystem.model.InventoryZone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryZoneRepository extends JpaRepository<InventoryZone, Long> {
    Optional<InventoryZone> findByZoneName(String zoneName);
}