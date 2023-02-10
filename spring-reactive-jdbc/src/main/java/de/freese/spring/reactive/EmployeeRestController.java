package de.freese.spring.reactive;

import jakarta.annotation.Resource;

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
    @Resource
    private EmployeeService service;

    @PutMapping("employee")
    public Publisher<Employee> createNewEmployee(@RequestBody final Employee newEmployee) {
        return this.service.createNewEmployee(newEmployee);
    }
}
