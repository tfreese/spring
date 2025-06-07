// Created: 26.08.2018
package de.freese.spring.thymeleaf.model;

/**
 * @author Thomas Freese
 */
public class PersonForm {
    private String firstName;
    private String lastName;

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
}
