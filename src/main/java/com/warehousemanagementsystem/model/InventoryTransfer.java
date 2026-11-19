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
@Table(name = "inventory_transfers")
public class InventoryTransfer {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "transfer_id", updatable = false, nullable = false)
    private UUID transferId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_rdc_id")
    private RegionalDistributionCenter sourceRdc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_store_id")
    private Store sourceStore;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_store_id", nullable = false)
    private Store destinationStore;

    @Enumerated(EnumType.STRING)
    @Column(name = "transfer_status", nullable = false, length = 50)
    private TransferStatus transferStatus = TransferStatus.PENDING;

    @Column(name = "ship_date")
    private OffsetDateTime shipDate;

    @Column(name = "estimated_arrival")
    private OffsetDateTime estimatedArrival;

    @Column(name = "actual_arrival")
    private OffsetDateTime actualArrival;

    @OneToMany(mappedBy = "transfer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InventoryTransferItem> transferItems;

    @Column(name = "created_by_employee_id")
    private Long createdByEmployeeId;

    @Column(name = "last_edited_by_employee_id")
    private Long lastEditedByEmployeeId;

    @Column(name = "last_edited_at")
    private OffsetDateTime lastEditedAt;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    public Long getCreatedByEmployeeId() {
        return createdByEmployeeId;
    }

    public void setCreatedByEmployeeId(Long createdByEmployeeId) {
        this.createdByEmployeeId = createdByEmployeeId;
    }

    public Long getLastEditedByEmployeeId() {
        return lastEditedByEmployeeId;
    }

    public void setLastEditedByEmployeeId(Long lastEditedByEmployeeId) {
        this.lastEditedByEmployeeId = lastEditedByEmployeeId;
    }

    public OffsetDateTime getLastEditedAt() {
        return lastEditedAt;
    }

    public void setLastEditedAt(OffsetDateTime lastEditedAt) {
        this.lastEditedAt = lastEditedAt;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    // Getters and Setters
}