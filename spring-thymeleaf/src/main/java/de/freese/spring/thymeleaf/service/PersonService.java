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

        this.persons.add(new Person("Bill", "Gates"));
        this.persons.add(new Person("Steve", "Jobs"));
    }

    @Secured("ROLE_ADMIN")
    public void addPerson(final Person newPerson) {
        String firstName = newPerson.getFirstName();
        String lastName = newPerson.getLastName();

        if ((firstName != null) && !firstName.isEmpty() && (lastName != null) && !lastName.isEmpty()) {
            this.persons.add(newPerson);
        }
        else {
            throw new IllegalArgumentException("Invalid Person Attributes");
        }
    }

    @Secured({"ROLE_ADMIN", "ROLE_USER"})
    public List<Person> getPersons() {
        return new ArrayList<>(this.persons);
    }
}
