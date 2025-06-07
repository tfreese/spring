// Created: 26.08.2018
package de.freese.spring.thymeleaf.model;

/**
 * @author Thomas Freese
 */
public class Person {
    private String firstName;
    private String lastName;

    public Person() {
        super();
    }

    public Person(final String firstName, final String lastName) {
        super();

        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("Person [firstName=");
        builder.append(firstName);
        builder.append(", lastName=");
        builder.append(lastName);
        builder.append("]");

        return builder.toString();
    }
}
