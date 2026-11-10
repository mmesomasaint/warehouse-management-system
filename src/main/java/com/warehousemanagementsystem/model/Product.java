package com.warehousemanagementsystem.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "product_id", updatable = false, nullable = false)
    private UUID productId;

    @Column(name = "sku", nullable = false, unique = true, length = 50)
    private String sku;

    @Column(name = "upc", unique = true, length = 50)
    private String upc;

    @Column(name = "product_name", nullable = false, length = 150)
    private String productName;

    @Column(name = "category", nullable = false, length = 50)
    private String category;

    @Column(name = "weight_lbs", precision = 8, scale = 2)
    private BigDecimal weightLbs;

    @Column(name = "is_hazardous", nullable = false)
    private Boolean isHazardous = false;

    @OneToMany(mappedBy = "product")
    private List<InventoryBalance> inventoryBalances;

    @OneToMany(mappedBy = "product")
    private List<RdcInventoryBalance> rdcInventoryBalances;

    @OneToMany(mappedBy = "product")
    private List<InventoryTransferItem> transferItems;

    @OneToMany(mappedBy = "product")
    private List<SalesOrderItem> orderItems;

    // Getters and Setters
}