package com.warehousemanagementsystem.security;

import com.warehousemanagementsystem.model.Employee;
import com.warehousemanagementsystem.model.security.EmployeeRole;
import com.warehousemanagementsystem.repository.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeUserDetailsServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeUserDetailsService userDetailsService;

    @Test
    void shouldMarkDeactivatedEmployeeAsDisabled() {
        Employee inactiveEmployee = new Employee();
        inactiveEmployee.setEmail("former.staff@warehouse.com");
        inactiveEmployee.setPasswordHash("encoded-hash");
        inactiveEmployee.setActive(false);
        inactiveEmployee.setRole(EmployeeRole.CASHIER);

        when(employeeRepository.findByEmail("former.staff@warehouse.com"))
                .thenReturn(Optional.of(inactiveEmployee));

        UserDetails userDetails = userDetailsService.loadUserByUsername("former.staff@warehouse.com");

        assertThat(userDetails.isEnabled()).isFalse();
        assertThat(userDetails.getAuthorities())
                .extracting("authority")
                .contains("sales:return", "sales:checkout");
    }
}