package de.freese.spring.reactive.model;

/**
 * @author Thomas Freese
 */
public class Department {
    private long id;
    private String name;

    public long getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public void setName(final String name) {
        this.name = name;
    }
}
