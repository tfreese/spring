/**
 *
 */
package de.freese.spring.reactive;

import javax.annotation.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import de.freese.spring.reactive.model.Employee;
import reactor.core.publisher.Mono;

/**
 * Die anderen REST-Methoden sind im {@link EmployeeRestController}.
 * 
 * @author Thomas Freese
 */
@Component
public class EmployeeHandler
{
    /**
     *
     */
    @Resource
    private EmployeeService service = null;

    /**
     * Erstellt ein neues {@link EmployeeHandler} Object.
     */
    public EmployeeHandler()
    {
        super();
    }

    /**
     * @param request {@link ServerRequest}
     * @return {@link Mono}
     */
    public Mono<ServerResponse> createNewEmployee(final ServerRequest request)
    {
        Mono<Employee> employeeMono = request.bodyToMono(Employee.class);

        Mono<Employee> employee = this.service.createNewEmployee(employeeMono);

        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(employee, Employee.class);
    }

    /**
     * @param request {@link ServerRequest}
     * @return {@link Mono}
     */
    public Mono<ServerResponse> deleteEmployee(final ServerRequest request)
    {
        Long id = Long.valueOf(request.pathVariable("id"));

        Mono<Void> employee = this.service.deleteEmployee(id);

        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).build(employee);
    }

    /**
     * @param request {@link ServerRequest}
     * @return {@link Mono}
     */
    public Mono<ServerResponse> getEmployee(final ServerRequest request)
    {
        String firstName = request.pathVariable("fn");
        String lastName = request.pathVariable("ln");

        Mono<Employee> employee = this.service.getEmployee(firstName, lastName);

        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(employee, Employee.class);
    }
}
