/**
 *
 */
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
     * @param employeeMono {@link Mono}
     * @return {@link Mono}
     */
    public Mono<Employee> createNewEmployee(Mono<Employee> employeeMono);

    /**
     * @param id long
     * @return {@link Mono}
     */
    public Mono<Void> deleteEmployee(long id);

    /**
     * @return {@link Flux}
     */
    public Flux<Department> getAllDepartments();

    /**
     * @return {@link Flux}
     */
    public Flux<Employee> getAllEmployees();

    /**
     * @param firstName String
     * @param lastName String
     * @return {@link Mono}
     */
    public Mono<Employee> getEmployee(String firstName, String lastName);
}
