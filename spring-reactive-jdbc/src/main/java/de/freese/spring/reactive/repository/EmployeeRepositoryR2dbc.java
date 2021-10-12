package de.freese.spring.reactive.repository;

import java.util.Objects;
import java.util.function.BiFunction;

import org.springframework.context.annotation.Profile;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;

import de.freese.spring.reactive.model.Department;
import de.freese.spring.reactive.model.Employee;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Thomas Freese
 */
@Repository
@Profile("r2dbc")
public class EmployeeRepositoryR2dbc implements EmployeeRepository
{
    /**
     * @author Thomas Freese
     */
    private static BiFunction<Row, RowMetadata, Department> DEPARTMENT_ROWMAPPER = (row, rowMetadata) -> {
        Department department = new Department();
        department.setId(row.get("department_id", Integer.class));
        department.setName(row.get("department_name", String.class));

        return department;
    };

    /**
     * @author Thomas Freese
     */
    private static BiFunction<Row, RowMetadata, Employee> EMPLOYEE_ROWMAPPER = (row, rowMetadata) -> {
        Employee employee = new Employee();
        employee.setId(row.get("employee_id", Integer.class));
        employee.setLastName(row.get("employee_lastname", String.class));
        employee.setFirstName(row.get("employee_firstname", String.class));
        employee.setDepartment(row.get("department_name", String.class));

        return employee;
    };

    /**
     *
     */
    private final DatabaseClient databaseClient;

    /**
     * Erstellt ein neues {@link EmployeeRepositoryR2dbc} Object.
     *
     * @param connectionFactory {@link ConnectionFactory}
     */
    public EmployeeRepositoryR2dbc(final ConnectionFactory connectionFactory)
    {
        super();

        this.databaseClient = DatabaseClient.create(Objects.requireNonNull(connectionFactory, "connectionFactory required"));
        // this.r2dbc = new R2dbc(Objects.requireNonNull(connectionFactory, "connectionFactory required"));
        // this.r2dbcTemplate = new R2dbcEntityTemplate(Objects.requireNonNull(connectionFactory, "connectionFactory required"));
    }

    /**
     * @see de.freese.spring.reactive.repository.EmployeeRepository#createNewEmployee(de.freese.spring.reactive.model.Employee)
     */
    @Override
    public Mono<Employee> createNewEmployee(final Employee newEmployee)
    {
        // @formatter:off
        final int departmentId = this.databaseClient.sql("SELECT department_id from department where department_name = :depName")
                .bind("depName", newEmployee.getDepartment())
                .map((row, rowMetadata) -> row.get("department_id", Integer.class))
                .one()
                .block()
                ;
        // @formatter:on

        // @formatter:off
        return this.databaseClient.sql("INSERT INTO employee (employee_lastname, employee_firstname, department_id) VALUES (:lastName, :firstName, :depId)")
                .filter((statement, executeFunction) -> statement.returnGeneratedValues("employee_id").execute())
                .bind("lastName", newEmployee.getLastName())
                .bind("firstName", newEmployee.getFirstName())
                .bind("depId", departmentId)
                .fetch()
                .first()
                .map(r -> {
                    int employeeId = (Integer) r.get("employee_id");
                    newEmployee.setId(employeeId);
                    return newEmployee;
                })
                ;
        // @formatter:on
    }

    /**
     * @see de.freese.spring.reactive.repository.EmployeeRepository#deleteEmployee(long)
     */
    @Override
    public Mono<Integer> deleteEmployee(final long id)
    {
        return this.databaseClient.sql("DELETE FROM employee WHERE employee_id = :id").bind("id", id).fetch().rowsUpdated();
    }

    /**
     * @see de.freese.spring.reactive.repository.EmployeeRepository#getAllDepartments()
     */
    @Override
    public Flux<Department> getAllDepartments()
    {
        // @formatter:off
        return this.databaseClient.sql("select * from department")
                //.filter((statement, executeFunction) -> statement.fetchSize(10).execute())
                .map(DEPARTMENT_ROWMAPPER)
                .all()
                ;
        // @formatter:on
    }

    /**
     * @see de.freese.spring.reactive.repository.EmployeeRepository#getAllEmployees()
     */
    @Override
    public Flux<Employee> getAllEmployees()
    {
        StringBuilder sql = new StringBuilder("select e.*, d.department_name");
        sql.append(" from employee e");
        sql.append(" INNER JOIN department d ON d.department_id = e.department_id");

        // @formatter:off
        return this.databaseClient.sql(sql.toString())
                //.filter((statement, executeFunction) -> statement.fetchSize(10).execute())
                .map(EMPLOYEE_ROWMAPPER)
                .all()
                ;
        // @formatter:on
    }

    /**
     * @see de.freese.spring.reactive.repository.EmployeeRepository#getEmployee(java.lang.String, java.lang.String)
     */
    @Override
    public Mono<Employee> getEmployee(final String lastName, final String firstName)
    {
        StringBuilder sql = new StringBuilder("select e.*, d.department_name");
        sql.append(" from employee e");
        sql.append(" INNER JOIN department d ON d.department_id = e.department_id");
        sql.append(" where");
        sql.append(" e.employee_lastname = :lastName");
        sql.append(" and e.employee_firstname = :firstName");

        // @formatter:off
        return this.databaseClient.sql(sql.toString())
                .bind("lastName", lastName)
                .bind("firstName", firstName)
                .map(EMPLOYEE_ROWMAPPER)
                .one()
                ;
        // @formatter:on
    }
}
