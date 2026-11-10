package com.warehousemanagementsystem.repository;

import com.warehousemanagementsystem.model.RegionalDistributionCenter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RegionalDistributionCenterRepository extends JpaRepository<RegionalDistributionCenter, UUID> {
    List<RegionalDistributionCenter> findByState_StateCode(String stateCode);
}