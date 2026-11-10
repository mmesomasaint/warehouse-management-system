package com.warehousemanagementsystem.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "inventory_balances")
public class InventoryBalance {

    @EmbeddedId
    private InventoryBalanceId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("storeId")
    @JoinColumn(name = "store_id")
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("productId")
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("zoneId")
    @JoinColumn(name = "zone_id")
    private InventoryZone zone;

    @Column(name = "quantity_on_hand", nullable = false)
    private Integer quantityOnHand = 0;

    @Column(name = "allocated_quantity", nullable = false)
    private Integer allocatedQuantity = 0;

    @Column(name = "minimum_safety_stock", nullable = false)
    private Integer minimumSafetyStock = 10;

    // Embedded ID Class
    @Embeddable
    public static class InventoryBalanceId implements Serializable {
        @Column(name = "store_id")
        private UUID storeId;

        @Column(name = "product_id")
        private UUID productId;

        @Column(name = "zone_id")
        private Long zoneId;

        public InventoryBalanceId() {}

        public InventoryBalanceId(UUID storeId, UUID productId, Long zoneId) {
            this.storeId = storeId;
            this.productId = productId;
            this.zoneId = zoneId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof InventoryBalanceId)) return false;
            InventoryBalanceId that = (InventoryBalanceId) o;
            return Objects.equals(storeId, that.storeId) &&
                  Objects.equals(productId, that.productId) &&
                  Objects.equals(zoneId, that.zoneId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(storeId, productId, zoneId);
        }
    }

    // Getters and Setters
}