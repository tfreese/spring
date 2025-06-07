package de.freese.spring.reactive;

import jakarta.annotation.Resource;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import de.freese.spring.reactive.model.Department;
import de.freese.spring.reactive.model.Employee;

/**
 * @author Thomas Freese
 */
@Component
@SuppressWarnings("java:S1172")
public class EmployeeHandler {
    @Resource
    private EmployeeService service;

    // Die REST-Methode 'createNewEmployee' wird im {@link EmployeeRestController} behandelt.
    //
    // public Mono<ServerResponse> createNewEmployee(final ServerRequest request) {
    // final Mono<Employee> employeeMono = request.bodyToMono(Employee.class);
    //
    // final Mono<Employee> employee = service.createNewEmployee(employeeMono);
    //
    // return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(employee, Employee.class);
    // }

    public Mono<ServerResponse> deleteEmployee(final ServerRequest request) {
        final long id = Long.parseLong(request.pathVariable("id"));

        final Mono<Long> count = service.deleteEmployee(id);

        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(count, Integer.class);
    }

    public Mono<ServerResponse> getAllDepartments(final ServerRequest request) {
        final Flux<Department> departments = service.getAllDepartments();

        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(departments, Department.class);
    }

    public Mono<ServerResponse> getAllEmployees(final ServerRequest request) {
        final Flux<Employee> employees = service.getAllEmployees();

        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(employees, Employee.class);
    }

    public Mono<ServerResponse> getEmployee(final ServerRequest request) {
        final String lastName = request.pathVariable("ln");
        final String firstName = request.pathVariable("fn");

        final Mono<Employee> employee = service.getEmployee(lastName, firstName);

        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(employee, Employee.class);
    }
}
