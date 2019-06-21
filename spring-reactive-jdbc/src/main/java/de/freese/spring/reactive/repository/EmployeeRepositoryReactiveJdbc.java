/**
 *
 */
package de.freese.spring.reactive.repository;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import de.freese.spring.reactive.model.Department;
import de.freese.spring.reactive.model.Employee;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Thomas Freese
 */
@Repository
@Profile("reactive-jdbc")
public class EmployeeRepositoryReactiveJdbc implements EmployeeRepository
{
    /**
     * Erstellt ein neues {@link EmployeeRepositoryReactiveJdbc} Object.
     */
    public EmployeeRepositoryReactiveJdbc()
    {
        super();
    }

    /**
     * @see de.freese.spring.reactive.repository.EmployeeRepository#createNewEmployee(reactor.core.publisher.Mono)
     */
    @Override
    public Mono<Employee> createNewEmployee(final Mono<Employee> employeeMono)
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see de.freese.spring.reactive.repository.EmployeeRepository#deleteEmployee(long)
     */
    @Override
    public Mono<Void> deleteEmployee(final long id)
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see de.freese.spring.reactive.repository.EmployeeRepository#getAllDepartments()
     */
    @Override
    public Flux<Department> getAllDepartments()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see de.freese.spring.reactive.repository.EmployeeRepository#getAllEmployees()
     */
    @Override
    public Flux<Employee> getAllEmployees()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see de.freese.spring.reactive.repository.EmployeeRepository#getEmployee(java.lang.String, java.lang.String)
     */
    @Override
    public Mono<Employee> getEmployee(final String firstName, final String lastName)
    {
        // TODO Auto-generated method stub
        return null;
    }
}
