// Created: 26.08.2018
package de.freese.spring.thymeleaf.facade;

import java.util.List;

import jakarta.annotation.Resource;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import de.freese.spring.thymeleaf.ThymeleafController;
import de.freese.spring.thymeleaf.model.Person;
import de.freese.spring.thymeleaf.model.PersonForm;
import de.freese.spring.thymeleaf.service.PersonService;

/**
 * @author Thomas Freese
 */
@ThymeleafController
public class PersonThymeleafController {
    @Resource
    private PersonService service;

    @GetMapping("/web/person/personList")
    public String personList(final Model model) {
        List<Person> persons = this.service.getPersons();
        model.addAttribute("persons", persons);

        return "/person/personList";
    }

    @PostMapping("/web/person/personAdd")
    public String savePerson(final Model model, @ModelAttribute("personForm") final PersonForm personForm) {
        String firstName = personForm.getFirstName();
        String lastName = personForm.getLastName();

        try {
            Person newPerson = new Person(firstName, lastName);
            this.service.addPerson(newPerson);

        }
        catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", "Invalid First- and Lastname !");

            return "/person/personAdd";
        }

        return "redirect:/web/person/personList";
    }

    @GetMapping("/web/person/personAdd")
    public String showAddPersonPage(final Model model) {
        PersonForm personForm = new PersonForm();
        model.addAttribute("personForm", personForm);

        return "/person/personAdd";
    }
}
