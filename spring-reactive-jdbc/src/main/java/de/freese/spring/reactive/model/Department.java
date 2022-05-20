package de.freese.spring.reactive.model;

/**
 * @author Thomas Freese
 */
public class Department
{
    /**
     *
     */
    private long id;
    /**
     *
     */
    private String name;

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
    public String getName()
    {
        return this.name;
    }

    /**
     * @param id long
     */
    public void setId(final long id)
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
