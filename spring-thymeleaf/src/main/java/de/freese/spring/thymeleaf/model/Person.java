// Created: 26.08.2018
package de.freese.spring.thymeleaf.model;

/**
 * @author Thomas Freese
 */
public class Person
{
    /**
     *
     */
    private String firstName;
    /**
     *
     */
    private String lastName;

    /**
     * Erstellt ein neues {@link Person} Object.
     */
    public Person()
    {
        super();
    }

    /**
     * Erstellt ein neues {@link Person} Object.
     *
     * @param firstName String
     * @param lastName String
     */
    public Person(final String firstName, final String lastName)
    {
        super();

        this.firstName = firstName;
        this.lastName = lastName;
    }

    /**
     * @return String
     */
    public String getFirstName()
    {
        return this.firstName;
    }

    /**
     * @return String
     */
    public String getLastName()
    {
        return this.lastName;
    }

    /**
     * @param firstName String
     */
    public void setFirstName(final String firstName)
    {
        this.firstName = firstName;
    }

    /**
     * @param lastName String
     */
    public void setLastName(final String lastName)
    {
        this.lastName = lastName;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("Person [firstName=");
        builder.append(this.firstName);
        builder.append(", lastName=");
        builder.append(this.lastName);
        builder.append("]");

        return builder.toString();
    }
}
