/**
 *
 */
package de.freese.spring.reactive.model;

/**
 * @author Thomas Freese
 */
public class Employee
{
    /**
     *
     */
    private String department;

    /**
     *
     */
    private String firstName = null;

    /**
     *
     */
    private int id = 0;

    /**
     *
     */
    private String lastName = null;

    /**
     * Erstellt ein neues {@link Employee} Object.
     */
    public Employee()
    {
        super();
    }

    /**
     * @return tString
     */
    public String getDepartment()
    {
        return this.department;
    }

    /**
     * @return String
     */
    public String getFirstName()
    {
        return this.firstName;
    }

    /**
     * @return int
     */
    public int getId()
    {
        return this.id;
    }

    /**
     * @return String
     */
    public String getLastName()
    {
        return this.lastName;
    }

    /**
     * @param department String
     */
    public void setDepartment(final String department)
    {
        this.department = department;
    }

    /**
     * @param firstName String
     */
    public void setFirstName(final String firstName)
    {
        this.firstName = firstName;
    }

    /**
     * @param id int
     */
    public void setId(final int id)
    {
        this.id = id;
    }

    /**
     * @param lastName String
     */
    public void setLastName(final String lastName)
    {
        this.lastName = lastName;
    }
}
