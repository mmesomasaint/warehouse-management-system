package com.warehousemanagementsystem.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "first_name", length = 50)
    private String firstName;

    @Column(name = "last_name", length = 50)
    private String lastName;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "is_contractor", nullable = false)
    private Boolean isContractor = false;

    @OneToMany(mappedBy = "customer")
    private List<SalesOrder> salesOrders;

    // Getters and Setters
}