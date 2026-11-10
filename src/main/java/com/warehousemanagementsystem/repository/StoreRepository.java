package com.warehousemanagementsystem.repository;

import com.warehousemanagementsystem.model.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StoreRepository extends JpaRepository<Store, UUID> {
    Optional<Store> findByStoreNumber(Integer storeNumber);
    List<Store> findByState_StateCode(String stateCode);
    List<Store> findByIsActiveTrue();
    List<Store> findByCityIgnoreCase(String city);
}