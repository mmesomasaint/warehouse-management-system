package com.warehousemanagementsystem.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "states")
public class State {

    @Id
    @Column(name = "state_code", length = 2, nullable = false)
    private String stateCode;

    @Column(name = "state_name", nullable = false, length = 50)
    private String stateName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id")
    private Region region;

    @OneToMany(mappedBy = "state")
    private List<Store> stores;

    @OneToMany(mappedBy = "state")
    private List<RegionalDistributionCenter> distributionCenters;

    // Getters and Setters
}