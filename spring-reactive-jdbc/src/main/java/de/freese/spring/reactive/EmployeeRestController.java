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
        MediaType.APPLICATION_JSON_VALUE
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

    /**
     * @param newEmployee {@link Employee}
     * @return {@link Publisher}
     */
    @PutMapping("employee")
    public Publisher<Employee> createNewEmployee(@RequestBody final Employee newEmployee)
    {
        Mono<Employee> employee = this.service.createNewEmployee(newEmployee);

        return employee;
    }
}
