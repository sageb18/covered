package com.sageb18.covered.service;

import com.sageb18.covered.model.Employee;
import com.sageb18.covered.repository.EmployeeRepository;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service // tells spring to register this class as a bean
public class EmployeeDetailsService  implements UserDetailsService {

    private final EmployeeRepository employeeRepository;

    public EmployeeDetailsService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public Employee loadUserByUsername(String email) {
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Employee not found: " + email));

        return employee;
    }
}
