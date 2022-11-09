// Created: 26.08.2018
package de.freese.spring.thymeleaf.model;

/**
 * @author Thomas Freese
 */
public class Person
{
    private String firstName;

    private String lastName;

    public Person()
    {
        super();
    }

    public Person(final String firstName, final String lastName)
    {
        super();

        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getFirstName()
    {
        return this.firstName;
    }

    public String getLastName()
    {
        return this.lastName;
    }

    public void setFirstName(final String firstName)
    {
        this.firstName = firstName;
    }

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
