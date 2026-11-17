package com.warehousemanagementsystem.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;

import java.math.BigDecimal;

@Service
public class EmployeeManagementService {

    @PreAuthorize("hasRole('ADMIN')") // Only ADMIN can adjust payroll
    public void updateEmployeeHourlyRate(Long employeeId, BigDecimal newRate) {
        // Payroll update logic...
    }
}