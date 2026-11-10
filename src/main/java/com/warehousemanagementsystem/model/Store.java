package com.warehousemanagementsystem.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "stores")
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "store_id", updatable = false, nullable = false)
    private UUID storeId;

    @Column(name = "store_number", nullable = false, unique = true)
    private Integer storeNumber;

    @Column(name = "store_name", nullable = false, length = 100)
    private String storeName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "state_code", nullable = false)
    private State state;

    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @Column(name = "postal_code", nullable = false, length = 20)
    private String postalCode;

    @Column(name = "street_address", nullable = false, length = 255)
    private String streetAddress;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @OneToOne(mappedBy = "store", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private StoreFulfillmentRouting fulfillmentRouting;

    @OneToMany(mappedBy = "store")
    private List<Employee> employees;

    @OneToMany(mappedBy = "store")
    private List<InventoryBalance> inventoryBalances;

    @OneToMany(mappedBy = "destinationStore")
    private List<InventoryTransfer> incomingTransfers;

    @OneToMany(mappedBy = "sourceStore")
    private List<InventoryTransfer> outgoingTransfers;

    @OneToMany(mappedBy = "store")
    private List<SalesOrder> salesOrders;

    // Getters and Setters
}