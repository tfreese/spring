/**
 *
 */
package de.freese.spring.reactive.repository;

import org.springframework.context.annotation.Profile;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Repository;
import de.freese.spring.reactive.model.Department;
import de.freese.spring.reactive.model.Employee;
import io.r2dbc.client.R2dbc;
import io.r2dbc.spi.ConnectionFactory;
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
     *
     */
    private final R2dbc r2dbc;

    /**
     *
     */
    private final R2dbcEntityTemplate r2dbcTemplate;

    /**
     * Erstellt ein neues {@link EmployeeRepositoryR2dbc} Object.
     *
     * @param connectionFactory {@link ConnectionFactory}
     */
    public EmployeeRepositoryR2dbc(final ConnectionFactory connectionFactory)
    {
        super();

        this.r2dbc = new R2dbc(connectionFactory);
        this.r2dbcTemplate = new R2dbcEntityTemplate(connectionFactory);
    }

    /**
     * @see de.freese.spring.reactive.repository.EmployeeRepository#createNewEmployee(de.freese.spring.reactive.model.Employee)
     */
    @Override
    public Mono<Employee> createNewEmployee(final Employee newEmployee)
    {
        // @formatter:off
//        this.r2dbc.withHandle(handle ->
//            handle
//                .select("SELECT department_id from department where department_name = ?", newEmployee.getDepartment())
//                .mapResult(result -> result.map((row, rowMetadata) -> row.get("department_id", Integer.class)))
//                )
//            .subscribe(System.out::println)
//        ;
        // Ergebnis ist 4 -> wie erwartet.

//        this.r2dbc.inTransaction(handle ->
//            handle
//                .execute("INSERT INTO employee (employee_firstname, employee_lastname, department_id) VALUES (?, ?, ?)",
//                    newEmployee.getFirstName()
//                    , newEmployee.getLastName()
//                    , 4)
//                )
//            .subscribe(System.out::println);
        // Ergebnis ist 1 -> Erwartet wird 7. -> execute liefert nur die affectedRows !!!
        // @formatter:on

        // @formatter:off
        //newEmployeeMono.map(newEmployee ->
        return this.r2dbc
                .withHandle(handle ->
                    handle
                        .select("SELECT department_id from department where department_name = ?", newEmployee.getDepartment())
                        .mapResult(result -> result.map((row, rowMetadata) -> row.get("department_id", Integer.class)))
                )
                //.single() // Es wird nur eine ID erwartet.
                .next() // Es wird nur eine ID erwartet.
                .flatMapMany(departmentId ->
                    this.r2dbc
                        .inTransaction(handle ->
                            handle
                                .createQuery("INSERT INTO employee (employee_firstname, employee_lastname, department_id) VALUES (?, ?, ?)")
                                    .bind(0, newEmployee.getFirstName())
                                    .bind(1, newEmployee.getLastName())
                                    .bind(2, departmentId)
                                    .mapRow(row -> row.get(0, Integer.class))
                                )
                )
                //.single() // Es wird nur ein Insert erwartet.
                .next() // Es wird nur ein Insert erwartet.
                .map(employeeId -> {
                    newEmployee.setId(employeeId);
                    return newEmployee;
                })
        //)
        ;
        // @formatter:on
    }

    /**
     * @see de.freese.spring.reactive.repository.EmployeeRepository#deleteEmployee(long)
     */
    @Override
    public Mono<Integer> deleteEmployee(final long id)
    {
        // @formatter:off
        return this.r2dbc.inTransaction(handle ->
            handle
                .execute("DELETE FROM employee WHERE employee_id = ?", id)
        )
        .single()
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
