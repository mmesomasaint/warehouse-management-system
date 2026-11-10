package com.warehousemanagementsystem.repository;

import com.warehousemanagementsystem.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    List<Employee> findByStore_StoreId(UUID storeId);
    List<Employee> findByRole(String role);
}