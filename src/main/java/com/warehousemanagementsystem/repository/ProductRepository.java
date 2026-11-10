package com.warehousemanagementsystem.repository;

import com.warehousemanagementsystem.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    Optional<Product> findBySku(String sku);
    Optional<Product> findByUpc(String upc);
    List<Product> findByCategory(String category);
    List<Product> findByIsHazardousTrue();
}