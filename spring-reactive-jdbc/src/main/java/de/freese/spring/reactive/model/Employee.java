package de.freese.spring.reactive.model;

import java.util.Objects;

/**
 * @author Thomas Freese
 */
public class Employee {
    private String department;

    private String firstName;

    private long id;

    private String lastName;

    public Employee() {
        super();
    }

    public Employee(final String lastName, final String firstName, final String department) {
        super();

        this.firstName = firstName;
        this.lastName = lastName;
        this.department = department;
    }

    public Employee(final String lastName, final String firstName, final String department, final long id) {
        this(lastName, firstName, department);

        this.id = id;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }

        Employee other = (Employee) obj;

        return (this.id == other.id) && Objects.equals(this.firstName, other.firstName) && Objects.equals(this.lastName, other.lastName) && Objects.equals(this.department, other.department);
    }

    public String getDepartment() {
        return this.department;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public long getId() {
        return this.id;
    }

    public String getLastName() {
        return this.lastName;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id, this.firstName, this.lastName, this.department);
    }

    public void setDepartment(final String department) {
        this.department = department;
    }

    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }

    @Override
    public String toString() {
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
