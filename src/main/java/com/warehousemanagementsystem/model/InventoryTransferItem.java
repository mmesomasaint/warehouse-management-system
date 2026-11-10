package com.warehousemanagementsystem.model;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "inventory_transfer_items")
public class InventoryTransferItem {

    @EmbeddedId
    private InventoryTransferItemId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("transferId")
    @JoinColumn(name = "transfer_id")
    private InventoryTransfer transfer;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("productId")
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "quantity_shipped", nullable = false)
    private Integer quantityShipped;

    @Column(name = "quantity_received")
    private Integer quantityReceived = 0;

    @Embeddable
    public static class InventoryTransferItemId implements Serializable {
        @Column(name = "transfer_id")
        private UUID transferId;

        @Column(name = "product_id")
        private UUID productId;

        public InventoryTransferItemId() {}

        public InventoryTransferItemId(UUID transferId, UUID productId) {
            this.transferId = transferId;
            this.productId = productId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof InventoryTransferItemId)) return false;
            InventoryTransferItemId that = (InventoryTransferItemId) o;
            return Objects.equals(transferId, that.transferId) && Objects.equals(productId, that.productId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(transferId, productId);
        }
    }

    // Getters and Setters
}