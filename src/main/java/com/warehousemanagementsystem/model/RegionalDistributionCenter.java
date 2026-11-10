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
@Table(name = "regional_distribution_centers")
public class RegionalDistributionCenter {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "rdc_id", updatable = false, nullable = false)
    private UUID rdcId;

    @Column(name = "rdc_name", nullable = false, length = 100)
    private String rdcName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "state_code", nullable = false)
    private State state;

    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @Column(name = "postal_code", nullable = false, length = 20)
    private String postalCode;

    @Column(name = "street_address", nullable = false, length = 255)
    private String streetAddress;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @OneToMany(mappedBy = "primaryRdc")
    private List<StoreFulfillmentRouting> primaryRoutedStores;

    @OneToMany(mappedBy = "secondaryRdc")
    private List<StoreFulfillmentRouting> secondaryRoutedStores;

    @OneToMany(mappedBy = "rdc")
    private List<RdcInventoryBalance> rdcInventoryBalances;

    @OneToMany(mappedBy = "sourceRdc")
    private List<InventoryTransfer> outgoingTransfers;

    // Getters and Setters
}