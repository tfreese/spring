package de.freese.spring.reactive.model;

import java.util.Objects;

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
    private String firstName;
    /**
     *
     */
    private long id;
    /**
     *
     */
    private String lastName;

    /**
     * Erstellt ein neues {@link Employee} Object.
     */
    public Employee()
    {
        super();
    }

    /**
     * Erstellt ein neues {@link Employee} Object.
     *
     * @param lastName String
     * @param firstName String
     * @param department String
     */
    public Employee(final String lastName, final String firstName, final String department)
    {
        super();

        this.firstName = firstName;
        this.lastName = lastName;
        this.department = department;
    }

    /**
     * Erstellt ein neues {@link Employee} Object.
     *
     * @param lastName String
     * @param firstName String
     * @param department String
     * @param id long
     */
    public Employee(final String lastName, final String firstName, final String department, final long id)
    {
        this(lastName, firstName, department);

        this.id = id;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj)
        {
            return true;
        }

        if ((obj == null) || (getClass() != obj.getClass()))
        {
            return false;
        }

        Employee other = (Employee) obj;

        if ((this.id != other.id) || !Objects.equals(this.firstName, other.firstName) || !Objects.equals(this.lastName, other.lastName)
                || !Objects.equals(this.department, other.department))
        {
            return false;
        }

        return true;
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
     * @return long
     */
    public long getId()
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
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return Objects.hash(this.id, this.firstName, this.lastName, this.department);
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
     * @param id long
     */
    public void setId(final long id)
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

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("Employee [");
        builder.append("id=").append(this.id);
        builder.append(", lastName=").append(this.lastName);
        builder.append(", firstName=").append(this.firstName);
        builder.append(", department=").append(this.department);
        builder.append("]");

        return builder.toString();
    }
}
