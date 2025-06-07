package de.freese.spring.testcontainers.model;

import java.util.Objects;

/**
 * @author Thomas Freese
 */
public class Person {
    private long id;
    private String name;

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof final Person data1)) {
            return false;
        }

        return getId() == data1.getId() && Objects.equals(getName(), data1.getName());
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName());
    }

    public void setId(final long id) {
        this.id = id;
    }

    public void setName(final String name) {
        this.name = name;
    }
}
