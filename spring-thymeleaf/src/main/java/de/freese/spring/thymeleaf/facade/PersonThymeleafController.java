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
        final List<Person> persons = service.getPersons();
        model.addAttribute("persons", persons);

        return "/person/personList";
    }

    @PostMapping("/web/person/personAdd")
    public String savePerson(final Model model, @ModelAttribute("personForm") final PersonForm personForm) {
        final String firstName = personForm.getFirstName();
        final String lastName = personForm.getLastName();

        try {
            final Person newPerson = new Person(firstName, lastName);
            service.addPerson(newPerson);

        }
        catch (IllegalArgumentException _) {
            model.addAttribute("errorMessage", "Invalid First- and Lastname !");

            return "/person/personAdd";
        }

        return "redirect:/web/person/personList";
    }

    @GetMapping("/web/person/personAdd")
    public String showAddPersonPage(final Model model) {
        final PersonForm personForm = new PersonForm();
        model.addAttribute("personForm", personForm);

        return "/person/personAdd";
    }
}
