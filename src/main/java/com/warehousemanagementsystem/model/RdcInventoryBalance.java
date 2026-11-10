package com.warehousemanagementsystem.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "rdc_inventory_balances")
public class RdcInventoryBalance {

    @EmbeddedId
    private RdcInventoryBalanceId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("rdcId")
    @JoinColumn(name = "rdc_id")
    private RegionalDistributionCenter rdc;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("productId")
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "quantity_in_stock", nullable = false)
    private Integer quantityInStock = 0;

    @Column(name = "quantity_allocated", nullable = false)
    private Integer quantityAllocated = 0;

    @Column(name = "aisle_bay_location", length = 50)
    private String aisleBayLocation;

    @Embeddable
    public static class RdcInventoryBalanceId implements Serializable {
        @Column(name = "rdc_id")
        private UUID rdcId;

        @Column(name = "product_id")
        private UUID productId;

        public RdcInventoryBalanceId() {}

        public RdcInventoryBalanceId(UUID rdcId, UUID productId) {
            this.rdcId = rdcId;
            this.productId = productId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof RdcInventoryBalanceId)) return false;
            RdcInventoryBalanceId that = (RdcInventoryBalanceId) o;
            return Objects.equals(rdcId, that.rdcId) && Objects.equals(productId, that.productId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(rdcId, productId);
        }
    }

    // Getters and Setters
}