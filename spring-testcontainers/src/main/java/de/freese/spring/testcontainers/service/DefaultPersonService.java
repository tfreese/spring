package de.freese.spring.testcontainers.service;

import java.util.List;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.freese.spring.testcontainers.model.Person;
import de.freese.spring.testcontainers.repository.PersonRepository;

/**
 * @author Thomas Freese
 */
@Service
public class DefaultPersonService implements PersonService {
    @Resource
    private PersonRepository personRepository;

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
