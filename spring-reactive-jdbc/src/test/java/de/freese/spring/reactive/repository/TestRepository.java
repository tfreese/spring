// Created: 24.06.2019
package de.freese.spring.reactive.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.test.StepVerifier;

import de.freese.spring.reactive.model.Employee;

/**
 * @author Thomas Freese
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.MethodName.class)
public interface TestRepository {
    @AfterEach
    default void afterEach() {
        doAfterEach();
    }

    @BeforeEach
    default void beforeEach() {
        doBeforeEach();
    }

    void doAfterEach();

    void doBeforeEach();

    EmployeeRepository getRepository();

    @Test
    default void testCreateNewEmployee() {
        final Employee newEmployee = new Employee("Foo", "Bar", "Dep1");

        getRepository().createNewEmployee(newEmployee)
                .as(StepVerifier::create)
                .expectNextMatches(emp -> emp.equals(new Employee("Foo", "Bar", "Dep1", 4)))
                .verifyComplete()
        ;

        getRepository().getAllEmployees()
                .as(StepVerifier::create)
                .expectNextCount(4)
                .verifyComplete()
        ;
    }

    @Test
    default void testDeleteEmployee() {
        getRepository().deleteEmployee(1)
                .as(StepVerifier::create)
                .expectNextMatches(count -> count == 1)
                .verifyComplete()
        ;

        getRepository().getAllEmployees()
                .as(StepVerifier::create)
                .expectNextCount(2)
                .verifyComplete()
        ;
    }

    @Test
    default void testGetAllDepartments() {
        getRepository().getAllDepartments()
                .as(StepVerifier::create)
                .expectNextCount(3)
                .verifyComplete()
        ;
    }

    @Test
    default void testGetEmployee() {
        getRepository().getEmployee("LastName1", "FirstName1")
                .as(StepVerifier::create)
                .expectNextMatches(emp -> emp.equals(new Employee("LastName1", "FirstName1", "Dep1", 1)))
                .verifyComplete()
        //.expectComplete()
        //.verify()
        ;
    }
}
