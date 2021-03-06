/**
 * Created: 26.08.2018
 */
package de.freese.spring.thymeleaf.model;

/**
 * @author Thomas Freese
 */
public class PersonForm
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
}
