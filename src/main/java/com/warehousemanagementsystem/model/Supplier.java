package com.warehousemanagementsystem.model;

import jakarta.persistence.*;

@Entity
@Table(name = "suppliers")
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "supplier_id")
    private Long supplierId;

    @Column(name = "company_name", nullable = false, length = 100)
    private String companyName;

    @Column(name = "contact_name", length = 100)
    private String contactName;

    @Column(name = "phone", length = 20)
    private String phone;

    // Getters and Setters
}