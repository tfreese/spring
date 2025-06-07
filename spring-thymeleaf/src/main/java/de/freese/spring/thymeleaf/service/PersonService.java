// Created: 04.09.2018
package de.freese.spring.thymeleaf.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;

import de.freese.spring.thymeleaf.model.Person;

/**
 * @author Thomas Freese
 */
@Service
public class PersonService {
    private final List<Person> persons = new ArrayList<>();

    public PersonService() {
        super();

        persons.add(new Person("Bill", "Gates"));
        persons.add(new Person("Steve", "Jobs"));
    }

    @Secured("ROLE_ADMIN")
    public void addPerson(final Person newPerson) {
        final String firstName = newPerson.getFirstName();
        final String lastName = newPerson.getLastName();

        if (firstName != null && !firstName.isEmpty() && lastName != null && !lastName.isEmpty()) {
            persons.add(newPerson);
        }
        else {
            throw new IllegalArgumentException("Invalid Person Attributes");
        }
    }

    @Secured({"ROLE_ADMIN", "ROLE_USER"})
    public List<Person> getPersons() {
        return new ArrayList<>(persons);
    }
}
