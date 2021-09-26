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
    /**
     * @param newEmployee {@link Employee}
     *
     * @return {@link Mono}
     */
    Mono<Employee> createNewEmployee(Employee newEmployee);

    /**
     * @param id long
     *
     * @return {@link Mono}
     */
    Mono<Integer> deleteEmployee(long id);

    /**
     * @return {@link Flux}
     */
    Flux<Department> getAllDepartments();

    /**
     * @return {@link Flux}
     */
    Flux<Employee> getAllEmployees();

    /**
     * @param firstName String
     * @param lastName String
     *
     * @return {@link Mono}
     */
    Mono<Employee> getEmployee(String firstName, String lastName);
}
