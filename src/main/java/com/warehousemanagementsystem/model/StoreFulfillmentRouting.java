package com.warehousemanagementsystem.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "store_fulfillment_routing")
public class StoreFulfillmentRouting {

    @Id
    @Column(name = "store_id")
    private UUID storeId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "store_id")
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "primary_rdc_id", nullable = false)
    private RegionalDistributionCenter primaryRdc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "secondary_rdc_id")
    private RegionalDistributionCenter secondaryRdc;

    // Getters and Setters
}