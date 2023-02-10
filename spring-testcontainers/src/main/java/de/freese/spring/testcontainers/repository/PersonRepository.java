package de.freese.spring.testcontainers.repository;

import java.util.List;

import de.freese.spring.testcontainers.model.Person;

/**
 * @author Thomas Freese
 */
public interface PersonRepository {
    List<Person> getAllOrderedById();

    void save(Person person);

    void saveAll(List<Person> persons);
}
