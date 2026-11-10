package com.warehousemanagementsystem.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "inventory_zones")
public class InventoryZone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "zone_id")
    private Long zoneId;

    @Column(name = "zone_name", nullable = false, length = 50)
    private String zoneName;

    @OneToMany(mappedBy = "zone")
    private List<InventoryBalance> inventoryBalances;

    // Getters and Setters
}