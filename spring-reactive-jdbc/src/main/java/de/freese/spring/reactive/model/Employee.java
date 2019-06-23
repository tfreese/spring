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
     * Erstellt ein neues {@link Employee} Object.
     *
     * @param id int
     * @param firstName String
     * @param lastName String
     * @param department String
     */
    public Employee(final int id, final String firstName, final String lastName, final String department)
    {
        super();

        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.department = department;
    }

    /**
     * Erstellt ein neues {@link Employee} Object.
     *
     * @param firstName String
     * @param lastName String
     * @param department String
     */
    public Employee(final String firstName, final String lastName, final String department)
    {
        super();

        this.firstName = firstName;
        this.lastName = lastName;
        this.department = department;
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

        if (obj == null)
        {
            return false;
        }

        if (getClass() != obj.getClass())
        {
            return false;
        }

        Employee other = (Employee) obj;

        if (this.id != other.id)
        {
            return false;
        }

        if (this.firstName == null)
        {
            if (other.firstName != null)
            {
                return false;
            }
        }
        else if (!this.firstName.equals(other.firstName))
        {
            return false;
        }

        if (this.lastName == null)
        {
            if (other.lastName != null)
            {
                return false;
            }
        }
        else if (!this.lastName.equals(other.lastName))
        {
            return false;
        }

        if (this.department == null)
        {
            if (other.department != null)
            {
                return false;
            }
        }
        else if (!this.department.equals(other.department))
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
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;

        result = (prime * result) + this.id;
        result = (prime * result) + ((this.firstName == null) ? 0 : this.firstName.hashCode());
        result = (prime * result) + ((this.lastName == null) ? 0 : this.lastName.hashCode());
        result = (prime * result) + ((this.department == null) ? 0 : this.department.hashCode());

        return result;
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

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("Employee [");
        builder.append("id=").append(this.id);
        builder.append(", firstName=").append(this.firstName);
        builder.append(", lastName=").append(this.lastName);
        builder.append(", department=").append(this.department);
        builder.append("]");

        return builder.toString();
    }
}
