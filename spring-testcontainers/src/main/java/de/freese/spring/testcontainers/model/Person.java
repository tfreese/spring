package de.freese.spring.testcontainers.model;

import java.util.Objects;

/**
 * @author Thomas Freese
 */
public class Person
{
    /**
     *
     */
    private long id;
    /**
     *
     */
    private String name;

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }

        if (!(o instanceof final Person data1))
        {
            return false;
        }

        return getId() == data1.getId() && Objects.equals(getName(), data1.getName());
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(getId(), getName());
    }

    /**
     * @return long
     */
    public long getId()
    {
        return this.id;
    }

    /**
     * @param id long
     */
    public void setId(final long id)
    {
        this.id = id;
    }

    /**
     * @return java.lang.String
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * @param name java.lang.String
     */
    public void setName(final String name)
    {
        this.name = name;
    }
}
