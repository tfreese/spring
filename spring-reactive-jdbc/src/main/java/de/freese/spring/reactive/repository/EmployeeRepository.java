package de.freese.spring.reactive.repository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import de.freese.spring.reactive.model.Department;
import de.freese.spring.reactive.model.Employee;

/**
 * @author Thomas Freese
 */
public interface EmployeeRepository {
    Mono<Employee> createNewEmployee(Employee newEmployee);

    Mono<Long> deleteEmployee(long id);

    Flux<Department> getAllDepartments();

    Flux<Employee> getAllEmployees();

    Mono<Employee> getEmployee(String lastName, String firstName);
}
