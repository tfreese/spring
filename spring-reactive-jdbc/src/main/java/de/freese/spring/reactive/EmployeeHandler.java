package de.freese.spring.reactive;

import jakarta.annotation.Resource;

import de.freese.spring.reactive.model.Department;
import de.freese.spring.reactive.model.Employee;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Thomas Freese
 */
@Component
public class EmployeeHandler
{
    @Resource
    private EmployeeService service;

    // Die REST-Methode 'createNewEmployee' wird im {@link EmployeeRestController} behandelt.
    //
    // public Mono<ServerResponse> createNewEmployee(final ServerRequest request)
    // {
    // Mono<Employee> employeeMono = request.bodyToMono(Employee.class);
    //
    // Mono<Employee> employee = this.service.createNewEmployee(employeeMono);
    //
    // return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(employee, Employee.class);
    // }

    public Mono<ServerResponse> deleteEmployee(final ServerRequest request)
    {
        long id = Long.parseLong(request.pathVariable("id"));

        Mono<Integer> count = this.service.deleteEmployee(id);

        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(count, Integer.class);
    }

    public Mono<ServerResponse> getAllDepartments(final ServerRequest request)
    {
        Flux<Department> departments = this.service.getAllDepartments();

        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(departments, Department.class);
    }

    public Mono<ServerResponse> getAllEmployees(final ServerRequest request)
    {
        Flux<Employee> employees = this.service.getAllEmployees();

        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(employees, Employee.class);
    }

    public Mono<ServerResponse> getEmployee(final ServerRequest request)
    {
        String lastName = request.pathVariable("ln");
        String firstName = request.pathVariable("fn");

        Mono<Employee> employee = this.service.getEmployee(lastName, firstName);

        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(employee, Employee.class);
    }
}
