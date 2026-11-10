package com.warehousemanagementsystem.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "regions")
public class Region {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "region_id")
    private Long regionId;

    @Column(name = "region_name", nullable = false, unique = true, length = 50)
    private String regionName;

    @OneToMany(mappedBy = "region")
    private List<State> states;

    // Getters and Setters
}