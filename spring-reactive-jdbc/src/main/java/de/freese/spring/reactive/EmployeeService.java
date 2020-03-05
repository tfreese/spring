/**
 *
 */
package de.freese.spring.reactive;

import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import de.freese.spring.reactive.model.Department;
import de.freese.spring.reactive.model.Employee;
import de.freese.spring.reactive.repository.EmployeeRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Thomas Freese
 */
@Service
public class EmployeeService
{
    /**
     *
     */
    @Resource
    private EmployeeRepository repository = null;

    /**
     * Erstellt ein neues {@link EmployeeService} Object.
     */
    public EmployeeService()
    {
        super();
    }

    /**
     * @param newEmployee {@link Employee}
     * @return {@link Mono}
     */
    @Transactional
    public Mono<Employee> createNewEmployee(final Employee newEmployee)
    {
        return this.repository.createNewEmployee(newEmployee);
    }

    /**
     * @param id long
     * @return {@link Mono}
     */
    @Transactional
    public Mono<Integer> deleteEmployee(final long id)
    {
        return this.repository.deleteEmployee(id);
    }

    /**
     * @return {@link Flux}
     */
    public Flux<Department> getAllDepartments()
    {
        return this.repository.getAllDepartments();
    }

    /**
     * @return {@link Flux}
     */
    public Flux<Employee> getAllEmployees()
    {
        return this.repository.getAllEmployees();
    }

    /**
     * @param firstName String
     * @param lastName String
     * @return {@link Mono}
     */
    public Mono<Employee> getEmployee(final String firstName, final String lastName)
    {
        return this.repository.getEmployee(firstName, lastName);
    }
}
