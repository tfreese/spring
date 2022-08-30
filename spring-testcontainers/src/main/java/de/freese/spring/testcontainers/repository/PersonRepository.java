package de.freese.spring.testcontainers.repository;

import java.util.List;

import de.freese.spring.testcontainers.model.Person;

/**
 * @author Thomas Freese
 */
public interface PersonRepository
{
    void save(Person person);

    void saveAll(List<Person> persons);

    List<Person> getAllOrderedById();
}
