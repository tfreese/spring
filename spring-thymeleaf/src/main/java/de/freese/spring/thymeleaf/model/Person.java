package de.freese.spring.thymeleaf.model;

/**
 * @author Thomas Freese
 * @since 26.08.2018
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
        return "Person [firstName=" + firstName
                + ", lastName=" + lastName
                + "]";
    }
}
