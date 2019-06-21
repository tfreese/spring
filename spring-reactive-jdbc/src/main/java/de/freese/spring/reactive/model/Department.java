/**
 *
 */
package de.freese.spring.reactive.model;

/**
 * @author Thomas Freese
 */
public class Department
{
    /**
     *
     */
    private int id = 0;

    /**
     *
     */
    private String name = null;

    /**
     * Erstellt ein neues {@link Department} Object.
     */
    public Department()
    {
        super();
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
    public String getName()
    {
        return this.name;
    }

    /**
     * @param id int
     */
    public void setId(final int id)
    {
        this.id = id;
    }

    /**
     * @param name String
     */
    public void setName(final String name)
    {
        this.name = name;
    }
}
