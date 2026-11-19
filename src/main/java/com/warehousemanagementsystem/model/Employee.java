package com.warehousemanagementsystem.model;

import com.warehousemanagementsystem.model.security.EmployeeRole;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "employees")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employee_id")
    private Long employeeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName = firstName + " " + lastName;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 50)
    private EmployeeRole role;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "employee_extra_permissions", joinColumns = @JoinColumn(name = "employee_id"))
    @Column(name = "permission", nullable = false, length = 100)
    private Set<String> extraPermissions = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "employee_revoked_permissions", joinColumns = @JoinColumn(name = "employee_id"))
    @Column(name = "permission", nullable = false, length = 100)
    private Set<String> revokedPermissions = new HashSet<>();

    @Column(name = "hourly_rate", precision = 10, scale = 2)
    private BigDecimal hourlyRate;

    @OneToMany(mappedBy = "employee")
    private List<SalesOrder> salesOrders;

    // Getters and Setters
}