/**
 *
 */
package de.freese.spring.reactive;

import javax.annotation.Resource;
import org.reactivestreams.Publisher;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import de.freese.spring.reactive.model.Employee;
import reactor.core.publisher.Mono;

/**
 * Die anderen REST-Methoden sind im {@link EmployeeRouter}.
 *
 * @author Thomas Freese
 */
@RestController
@RequestMapping(path = "/", produces =
{
        MediaType.APPLICATION_JSON_UTF8_VALUE
})
public class EmployeeRestController
{
    /**
     *
     */
    @Resource
    private EmployeeService service = null;

    /**
     * Erstellt ein neues {@link EmployeeRestController} Object.
     */
    public EmployeeRestController()
    {
        super();
    }

    // @RequestParam final Optional<String> name

    // public Mono<ServerResponse> getAllDepartments(final ServerRequest request)
    // {
    // Flux<Department> departments = repository.getAllDepartments();
    // return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(departments, Department.class);
    // }

    /**
     * @param newEmployee {@link Employee}
     * @return {@link Publisher}
     */
    @PutMapping("employee")
    // @PutMapping(path = "employee", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Publisher<Employee> createNewEmployee(@RequestBody final Employee newEmployee)
    {
        Mono<Employee> employee = this.service.createNewEmployee(newEmployee);

        return employee;
    }

    // /**
    // * @return {@link Publisher}
    // */
    // // @GetMapping("departments")
    // public Publisher<Department> getAllDepartments()
    // {
    // Flux<Department> departments = this.service.getAllDepartments();
    //
    // return departments;
    // }

    // /**
    // * @return {@link Publisher}
    // */
    // @GetMapping("employees")
    // public Publisher<Employee> getAllEmployees()
    // {
    // Flux<Employee> employees = this.service.getAllEmployees();
    //
    // return employees;
    // }
}
