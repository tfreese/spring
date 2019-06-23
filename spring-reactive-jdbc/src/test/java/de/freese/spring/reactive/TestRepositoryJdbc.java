/**
 * Created: 23.06.2019
 */

package de.freese.spring.reactive;

import javax.annotation.Resource;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import de.freese.spring.reactive.model.Department;
import de.freese.spring.reactive.model.Employee;
import de.freese.spring.reactive.repository.EmployeeRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * @author Thomas Freese
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("jdbc")
@Disabled
public class TestRepositoryJdbc
{
    /**
     *
     */
    @Resource
    private EmployeeRepository repository = null;

    /**
     * Erstellt ein neues {@link TestRepositoryJdbc} Object.
     */
    public TestRepositoryJdbc()
    {
        super();
    }

    /**
      *
     */
    @Test
    public void createNewEmployee()
    {
        Employee newEmployee = new Employee("Foo", "Bar", "Manufacturing");
        Employee expected = new Employee(7, "Foo", "Bar", "Manufacturing");

        Mono<Employee> returned = this.repository.createNewEmployee(Mono.just(newEmployee));

        // @formatter:off
        returned
            .as(StepVerifier::create)
            .expectNextCount(1)
            .expectNextMatches(emp -> emp.equals(expected))
            .verifyComplete()
            ;
        // @formatter:on

        Flux<Employee> employees = this.repository.getAllEmployees();

        // @formatter:off
        employees
            .as(StepVerifier::create)
            .expectNextCount(7)
            .verifyComplete()
            ;
        // @formatter:on
    }

    /**
     *
    */
    @Test
    public void deleteEmployee()
    {
        Mono<Void> employee = this.repository.deleteEmployee(1);

        // @formatter:off
        employee
            .as(StepVerifier::create)
            .expectComplete()
            ;
        // @formatter:on

        Flux<Employee> employees = this.repository.getAllEmployees();

        // @formatter:off
        employees
            .as(StepVerifier::create)
            .expectNextCount(5)
            .verifyComplete()
            ;
        // @formatter:on
    }

    /**
     *
     */
    @Test
    public void getAllDepartments()
    {
        Flux<Department> departments = this.repository.getAllDepartments();

        // @formatter:off
        departments
            .as(StepVerifier::create)
            .expectNextCount(5)
            .verifyComplete()
            ;
        // @formatter:on
    }

    /**
    *
    */
    @Test
    public void getAllEmployees()
    {
        Flux<Employee> employees = this.repository.getAllEmployees();

        // @formatter:off
        employees
            .as(StepVerifier::create)
            .expectNextCount(6)
            .verifyComplete()
            ;
        // @formatter:on
    }

    /**
     *
     */
    @Test
    public void getEmployee()
    {
        Mono<Employee> employee = this.repository.getEmployee("Sally", "Wilson");

        // @formatter:off
        employee
            .as(StepVerifier::create)
            //.expectNextCount(1)
            .expectNextMatches(emp -> emp.equals(new Employee(3, "Sally","Wilson", "Human Resources")))
            .verifyComplete()
            ;
        // @formatter:on
    }
}
