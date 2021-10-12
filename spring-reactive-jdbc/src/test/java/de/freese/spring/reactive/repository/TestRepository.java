// Created: 24.06.2019
package de.freese.spring.reactive.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;

import de.freese.spring.reactive.model.Employee;
import reactor.test.StepVerifier;

/**
 * @author Thomas Freese
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.MethodName.class)
public interface TestRepository
{
    /**
     *
     */
    @AfterEach
    default void afterEach()
    {
        deleteDatabase();
    }

    /**
     *
     */
    @BeforeEach
    default void beforeEach()
    {
        fillDatabase();
    }

    /**
     *
     */
    void deleteDatabase();

    /**
     *
     */
    void fillDatabase();

    /**
     * @return {@link EmployeeRepository}
     */
    EmployeeRepository getRepository();

    /**
      *
     */
    @Test
    default void testCreateNewEmployee()
    {
        Employee newEmployee = new Employee("Foo", "Bar", "Dep1");

        // @formatter:off
        getRepository().createNewEmployee(newEmployee)
            .as(StepVerifier::create)
            .expectNextMatches(emp -> emp.equals(new Employee("Foo", "Bar", "Dep1", 4)))
            .verifyComplete()
            ;
        // @formatter:on

        // @formatter:off
        getRepository().getAllEmployees()
            .as(StepVerifier::create)
            .expectNextCount(4)
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
        // @formatter:off
        getRepository().deleteEmployee(1)
            .as(StepVerifier::create)
            .expectNextMatches(count -> count == 1)
            .verifyComplete()
            ;
        // @formatter:on

        // @formatter:off
        getRepository().getAllEmployees()
            .as(StepVerifier::create)
            .expectNextCount(2)
            .verifyComplete()
            ;
        // @formatter:on
    }

    /**
     *
     */
    @Test
    default void testGetAllDepartments()
    {
        // @formatter:off
        getRepository().getAllDepartments()
            .as(StepVerifier::create)
            .expectNextCount(3)
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
        // @formatter:off
        getRepository().getEmployee("LastName1", "FirstName1")
            .as(StepVerifier::create)
            .expectNextMatches(emp -> emp.equals(new Employee("LastName1", "FirstName1", "Dep1", 1)))
            .verifyComplete()
            //.expectComplete()
            //.verify()
            ;
        // @formatter:on
    }
}