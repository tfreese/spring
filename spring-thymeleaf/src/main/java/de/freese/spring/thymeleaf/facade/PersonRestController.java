// Created: 05.09.2018
package de.freese.spring.thymeleaf.facade;

import java.util.List;

import jakarta.annotation.Resource;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.freese.spring.thymeleaf.model.Person;
import de.freese.spring.thymeleaf.service.PersonService;

/**
 * @author Thomas Freese
 */
@RestController
@RequestMapping("rest")
// @RequestMapping(path = "rest", produces =
// {
// MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE
// })
public class PersonRestController {
    @Resource
    private PersonService service;

    @GetMapping("/createError")
    public void createError() {
        // throw new IllegalArgumentException("Test Exception");
        throw new IllegalStateException("Test Exception");
    }

    @GetMapping("/person/personList")
    public List<Person> personList() {
        return service.getPersons();
    }

    @PostMapping(path = "/person/personAdd", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void savePerson(@RequestBody final Person newPerson) {
        service.addPerson(newPerson);
    }
}
