// Created: 30.06.2025
package com.spring.neo4j.repository;

import java.util.List;

import com.spring.neo4j.model.Person;
import org.springframework.data.neo4j.repository.Neo4jRepository;

/**
 * @author Thomas Freese
 */
public interface PersonRepository extends Neo4jRepository<Person, Long> {
    // Person findOneByName(String name);
    Person findByName(String name);

    List<Person> findByTeammatesName(String name);
}
