// Created: 30.06.2025
package com.spring.neo4j.model;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

/**
 * @author Thomas Freese
 */
@Node("PERSON") // Must match MyLabels!
public class Person {
    private final String name;
    @Id
    @GeneratedValue
    private Long id;
    @Relationship(type = "TEAMMATE", direction = Relationship.Direction.INCOMING)
    private Set<Person> teammates;

    public Person(final String name) {
        super();

        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Set<Person> getTeammates() {
        return Set.copyOf(teammates);
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(getClass().getSimpleName());
        sb.append(" [");
        sb.append("id=").append(id);
        sb.append(", name='").append(name).append('\'');
        sb.append(", teammates=").append(Optional.ofNullable(teammates)
                .orElse(Set.of())
                .stream()
                .map(Person::getName)
                .sorted()
                .collect(Collectors.joining(",", "'", "'")));

        sb.append(']');

        return sb.toString();
    }

    public void worksWith(final Person person) {
        if (teammates == null) {
            teammates = new HashSet<>();
        }

        teammates.add(person);
    }
}
