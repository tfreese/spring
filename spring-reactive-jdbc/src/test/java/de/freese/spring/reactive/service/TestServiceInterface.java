// Created: 24.06.2019
package de.freese.spring.reactive.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import de.freese.spring.reactive.EmployeeService;
import de.freese.spring.reactive.model.Department;
import de.freese.spring.reactive.model.Employee;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * @author Thomas Freese
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.MethodName.class)
public interface TestServiceInterface
{
    /**
     *
     */
    @AfterEach
    default void afterEach()
    {
        getJdbcTemplate().execute("DROP TABLE employee");
        getJdbcTemplate().execute("DROP TABLE department");
    }

    /**
     *
     */
    @BeforeEach
    default void beforeEach()
    {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("sql/schema-h2.sql"));
        populator.addScript(new ClassPathResource("sql/data.sql"));
        populator.execute(getJdbcTemplate().getDataSource());
    }

    /**
     * @return {@link JdbcTemplate}
     */
    JdbcTemplate getJdbcTemplate();

    /**
     * @return {@link EmployeeService}
     */
    EmployeeService getService();

    /**
      *
     */
    @Test
    default void testCreateNewEmployee()
    {
        Employee newEmployee = new Employee("Foo", "Bar", "Manufacturing");
        Employee expected = new Employee(7, "Foo", "Bar", "Manufacturing");

        Mono<Employee> returned = getService().createNewEmployee(newEmployee);

        // @formatter:off
        returned
            .as(StepVerifier::create)
            .expectNextMatches(emp -> emp.equals(expected))
            .verifyComplete()
            ;
        // @formatter:on

        Flux<Employee> employees = getService().getAllEmployees();

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
    default void testDeleteEmployee()
    {
        Mono<Integer> employee = getService().deleteEmployee(1);

        // @formatter:off
        employee
            .as(StepVerifier::create)
            .expectNextCount(1)
            .expectNextMatches(count -> count == 1)
            ;
        // @formatter:on

        Flux<Employee> employees = getService().getAllEmployees();

        // @formatter:off
        employees
            .as(StepVerifier::create)
            .expectNextCount(5)
//            .verifyComplete()
            ;
        // @formatter:on
    }

    /**
     *
     */
    @Test
    default void testGetAllDepartments()
    {
        Flux<Department> departments = getService().getAllDepartments();

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
    default void testGetAllEmployees()
    {
        Flux<Employee> employees = getService().getAllEmployees();

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
    default void testGetEmployee()
    {
        Mono<Employee> employee = getService().getEmployee("Sally", "Wilson");

        // @formatter:off
        employee
            .as(StepVerifier::create)
            .expectNextCount(1)
            .expectNextMatches(emp -> emp.equals(new Employee(3, "Sally","Wilson", "Human Resources")))
//            .verifyComplete()
            ;
        // @formatter:on
    }
}