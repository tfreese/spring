/**
 *
 */
package de.freese.spring.reactive.repository;

import java.util.Objects;
import javax.sql.DataSource;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import de.freese.spring.reactive.model.Department;
import de.freese.spring.reactive.model.Employee;
import io.r2dbc.client.R2dbc;
import io.r2dbc.jdbc.JdbcConnectionFactoryProvider;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Thomas Freese
 */
@Repository
@Profile("jdbc-reactive")
public class EmployeeRepositoryReactiveJdbc implements EmployeeRepository
{
    /**
     *
     */
    private final ConnectionFactory connectionFactory;

    /**
     *
     */
    private final R2dbc r2dbc;

    /**
     * Erstellt ein neues {@link EmployeeRepositoryReactiveJdbc} Object.
     *
     * @param dataSource {@link DataSource}
     */
    public EmployeeRepositoryReactiveJdbc(final DataSource dataSource)
    {
        super();

        // @formatter:off
        this.connectionFactory = ConnectionFactories.get(ConnectionFactoryOptions.builder()
                .option(JdbcConnectionFactoryProvider.DATASOURCE, Objects.requireNonNull(dataSource, "dataSource required"))
                .build()
                )
                ;

        this.r2dbc = new R2dbc(this.connectionFactory);
        // @formatter:on
    }

    /**
     * @see de.freese.spring.reactive.repository.EmployeeRepository#createNewEmployee(reactor.core.publisher.Mono)
     */
    @Override
    public Mono<Employee> createNewEmployee(final Mono<Employee> employeeMono)
    {
        // @formatter:off
        employeeMono.map(employee ->
            this.r2dbc
                .withHandle(handle ->
                    handle
                        .select("SELECT department_id from department where department_name = ?", employee.getDepartment())
                        .mapResult(result -> result.map((row, rowMetadata) -> row.get("department_id", Integer.class)))
                )
                .map(departmentId ->
                    this.r2dbc
                        .inTransaction(handle ->
                            handle
                                .execute("INSERT INTO employee (employee_firstname, employee_lastname, department_id) VALUES (?, ?, ?)"
                                                                                              , employee.getFirstName()
                                                                                              , employee.getLastName()
                                                                                              , departmentId)
                                )
                )
                //.then(Mono.of())


        )
        ;

        Employee employee = employeeMono.block();

        return getEmployee(employee.getFirstName(), employee.getLastName());
        // @formatter:on

        // r2dbc.inTransaction(handle ->
        // handle.execute("INSERT INTO test VALUES ($1)", 100))
        //
        // .thenMany(r2dbc.inTransaction(handle ->
        // handle.select("SELECT value FROM test")
        // .mapResult(result -> result.map((row, rowMetadata) -> row.get("value", Integer.class)))))
        //
        // .subscribe(System.out::println);

        // return null;
    }

    /**
     * @see de.freese.spring.reactive.repository.EmployeeRepository#deleteEmployee(long)
     */
    @Override
    public Mono<Void> deleteEmployee(final long id)
    {
        // @formatter:off
        return this.r2dbc.inTransaction(handle ->
            handle
                .execute("DELETE FROM employee WHERE employee_id = ?", id)
        )
        .then()
        ;
        // @formatter:on
    }

    /**
     * @see de.freese.spring.reactive.repository.EmployeeRepository#getAllDepartments()
     */
    @Override
    public Flux<Department> getAllDepartments()
    {
        // @formatter:off
        return this.r2dbc.withHandle(handle ->
            handle
                .select("select * from department")
                .mapRow(row -> {
                                    Department department = new Department();
                                    department.setId(row.get("department_id", Integer.class));
                                    department.setName(row.get("department_name", String.class));

                                    return department;
                               }
                )
        )
        ;
        // @formatter:on
    }

    /**
     * @see de.freese.spring.reactive.repository.EmployeeRepository#getAllEmployees()
     */
    @Override
    public Flux<Employee> getAllEmployees()
    {
        StringBuilder sql = new StringBuilder("select e.*, d.department_name from employee e");
        sql.append(" INNER JOIN department d ON e.department_id = d.department_id");

        // @formatter:off
        return this.r2dbc.withHandle(handle ->
            handle
                .select(sql.toString())
                .mapRow(row -> {
                                    Employee employee = new Employee();
                                    employee.setId(row.get("employee_id", Integer.class));
                                    employee.setDepartment(row.get("department_name", String.class));
                                    employee.setFirstName(row.get("employee_firstname", String.class));
                                    employee.setLastName(row.get("employee_lastname", String.class));

                                    return employee;
                               }
                )
        )
        ;
        // @formatter:on
    }

    /**
     * @see de.freese.spring.reactive.repository.EmployeeRepository#getEmployee(java.lang.String, java.lang.String)
     */
    @Override
    public Mono<Employee> getEmployee(final String firstName, final String lastName)
    {
        StringBuilder sql = new StringBuilder("select e.*, d.department_name from employee e");
        sql.append(" INNER JOIN department d ON e.department_id = d.department_id");
        sql.append(" where");
        sql.append(" e.employee_lastname = ?");
        sql.append(" and e.employee_firstname = ?");

        // @formatter:off
        return this.r2dbc.withHandle(handle ->
            handle
                .select(sql.toString(), lastName, firstName)
                .mapRow(row -> {
                                    Employee employee = new Employee();
                                    employee.setId(row.get("employee_id", Integer.class));
                                    employee.setDepartment(row.get("department_name", String.class));
                                    employee.setFirstName(row.get("employee_firstname", String.class));
                                    employee.setLastName(row.get("employee_lastname", String.class));

                                    return employee;
                                }

                )
       )
       .single()
       ;
        // @formatter:on
    }
}
