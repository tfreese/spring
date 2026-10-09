package de.freese.spring.testcontainers.service;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.freese.spring.testcontainers.model.Person;
import de.freese.spring.testcontainers.repository.PersonRepository;

/**
 * @author Thomas Freese
 */
@Service
public class DefaultPersonService implements PersonService {
    private final PersonRepository personRepository;

    public DefaultPersonService(final PersonRepository personRepository) {
        super();

        this.personRepository = Objects.requireNonNull(personRepository, "personRepository required");
    }

    @Override
    public List<Person> getAll() {
        return personRepository.getAllOrderedById();
    }

    @Override
    @Transactional
    public void save(final Person person) {
        personRepository.save(person);
    }

    @Override
    @Transactional
    public void saveAll(final List<Person> persons) {
        personRepository.saveAll(persons);
    }

    @Override
    @Transactional
    public void saveAllWithException(final List<Person> persons) {
        personRepository.saveAll(persons);

        throw new RuntimeException("saveAllWithException");
    }
}
