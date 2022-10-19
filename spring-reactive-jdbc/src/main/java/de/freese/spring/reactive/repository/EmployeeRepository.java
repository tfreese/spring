package de.freese.spring.reactive.repository;

import de.freese.spring.reactive.model.Department;
import de.freese.spring.reactive.model.Employee;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Thomas Freese
 */
public interface EmployeeRepository
{
    Mono<Employee> createNewEmployee(Employee newEmployee);

    Mono<Long> deleteEmployee(long id);

    Flux<Department> getAllDepartments();

    Flux<Employee> getAllEmployees();

    Mono<Employee> getEmployee(String lastName, String firstName);
}
