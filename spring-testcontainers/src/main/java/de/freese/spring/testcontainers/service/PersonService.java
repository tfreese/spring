package de.freese.spring.testcontainers.service;

import java.util.List;

import de.freese.spring.testcontainers.model.Person;

/**
 * @author Thomas Freese
 */
public interface PersonService {
    List<Person> getAll();

    void save(Person person);

    void saveAll(List<Person> persons);

    void saveAllWithException(List<Person> persons);
}
