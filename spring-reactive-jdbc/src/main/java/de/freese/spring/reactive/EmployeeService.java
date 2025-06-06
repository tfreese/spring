package de.freese.spring.reactive;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import de.freese.spring.reactive.model.Department;
import de.freese.spring.reactive.model.Employee;
import de.freese.spring.reactive.repository.EmployeeRepository;

/**
 * @author Thomas Freese
 */
@Service
public class EmployeeService {
    @Resource
    private EmployeeRepository repository;

    @Transactional
    public Mono<Employee> createNewEmployee(final Employee newEmployee) {
        return repository.createNewEmployee(newEmployee);
    }

    @Transactional
    public Mono<Long> deleteEmployee(final long id) {
        return repository.deleteEmployee(id);
    }

    public Flux<Department> getAllDepartments() {
        return repository.getAllDepartments();
    }

    public Flux<Employee> getAllEmployees() {
        return repository.getAllEmployees();
    }

    public Mono<Employee> getEmployee(final String lastName, final String firstName) {
        return repository.getEmployee(lastName, firstName);
    }
}
