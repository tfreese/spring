package de.freese.spring.reactive;

import java.util.Objects;

import org.reactivestreams.Publisher;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.freese.spring.reactive.model.Employee;

/**
 * Die anderen REST-Methoden sind im {@link EmployeeRouter}.
 *
 * @author Thomas Freese
 */
@RestController
@RequestMapping(path = "/", produces = {MediaType.APPLICATION_JSON_VALUE})
public class EmployeeRestController {
    private final EmployeeService service;

    public EmployeeRestController(final EmployeeService service) {
        super();

        this.service = Objects.requireNonNull(service, "service required");
    }

    @PutMapping("employee")
    public Publisher<Employee> createNewEmployee(@RequestBody final Employee newEmployee) {
        return service.createNewEmployee(newEmployee);
    }
}
