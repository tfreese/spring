package de.freese.spring.reactive.repository;
//
// import java.util.Objects;
// import java.util.function.BiFunction;
//
// import de.freese.spring.reactive.model.Department;
// import de.freese.spring.reactive.model.Employee;
// import io.r2dbc.client.R2dbc;
// import io.r2dbc.spi.ConnectionFactory;
// import io.r2dbc.spi.Row;
// import io.r2dbc.spi.RowMetadata;
// import reactor.core.publisher.Flux;
// import reactor.core.publisher.Mono;

import java.util.function.BiFunction;

import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import de.freese.spring.reactive.model.Department;
import de.freese.spring.reactive.model.Employee;

/**
 * @author Thomas Freese
 */
// @Repository
// @Profile("r2dbc")
//
public class EmployeeRepositoryR2dbcClient implements EmployeeRepository {
    private static final BiFunction<Row, RowMetadata, Department> DEPARTMENT_ROWMAPPER = (row, rowMetadata) -> {
        Department department = new Department();
        department.setId(row.get("department_id", Long.class));
        department.setName(row.get("department_name", String.class));

        return department;
    };

    private static final BiFunction<Row, RowMetadata, Employee> EMPLOYEE_ROWMAPPER = (row, rowMetadata) -> {
        Employee employee = new Employee();
        employee.setId(row.get("employee_id", Long.class));
        employee.setLastName(row.get("employee_lastname", String.class));
        employee.setFirstName(row.get("employee_firstname", String.class));
        employee.setDepartment(row.get("department_name", String.class));

        return employee;
    };

    // private final R2dbc r2dbc;

    // private final R2dbcEntityTemplate r2dbcTemplate;

    public EmployeeRepositoryR2dbcClient(final ConnectionFactory connectionFactory) {
        super();

        // r2dbc = new R2dbc(Objects.requireNonNull(connectionFactory, "connectionFactory required"));
        // r2dbcTemplate = new R2dbcEntityTemplate(Objects.requireNonNull(connectionFactory, "connectionFactory required"));
    }

    @Override
    public Mono<Employee> createNewEmployee(final Employee newEmployee) {
        // r2dbc.withHandle(handle -> handle.select("SELECT department_id from department where department_name = ?", newEmployee.getDepartment())
        // .mapResult(result -> result.map((row, rowMetadata) -> row.get("department_id", Integer.class)))).subscribe(System.out::println);
        // // Ergebnis ist 1 -> wie erwartet.
        //
        // r2dbc.inTransaction(handle -> handle.execute("INSERT INTO employee (employee_firstname, employee_lastname, department_id) VALUES (?, ?, ?)",
        // newEmployee.getFirstName(), newEmployee.getLastName(), 1)).subscribe(System.out::println);
        // // Ergebnis ist 1 -> Erwartet wird 4. -> execute liefert nur die affectedRows !!!

        // return r2dbc
        //         .withHandle(handle ->
        //                 handle
        //                         .select("SELECT department_id from department where department_name = ?", newEmployee.getDepartment())
        //                         .mapResult(result -> result.map((row, rowMetadata) -> row.get("department_id", Integer.class)))
        //         )
        //         .log()
        //         .single() // Es wird nur eine ID erwartet.
        //         // .next() // Es wird nur eine ID erwartet.
        //         .flatMapMany(departmentId ->
        //                 r2dbc
        //                         .inTransaction(handle ->
        //                                 handle
        //                                         .createQuery("INSERT INTO employee (employee_firstname, employee_lastname, department_id) VALUES (?, ?, ?)")
        //                                         .bind(0, newEmployee.getFirstName())
        //                                         .bind(1, newEmployee.getLastName())
        //                                         .bind(2, departmentId)
        //                                         .mapRow(row -> row.get(0, Integer.class))
        //                         )
        //         )
        //         .log()
        //         .single() // Es wird nur ein Insert erwartet.
        //         // .next() // Es wird nur ein Insert erwartet.
        //         .map(employeeId -> {
        //             newEmployee.setId(employeeId);
        //             return newEmployee;
        //         })
        //         //)
        //         ;
        return Mono.empty();
    }

    @Override
    public Mono<Long> deleteEmployee(final long id) {
        // return r2dbc.inTransaction(handle ->
        //                 handle
        //                         .execute("DELETE FROM employee WHERE employee_id = ?", id)
        //         )
        //         .single()
        //         .map(i -> (long) i)
        //         ;

        return Mono.empty();
    }

    @Override
    public Flux<Department> getAllDepartments() {
        // return r2dbc.withHandle(handle ->
        //         handle
        //                 .select("select * from department")
        //                 .mapRow(DEPARTMENT_ROWMAPPER)
        // )
        //         ;

        return Flux.empty();
    }

    @Override
    public Flux<Employee> getAllEmployees() {
        // final String sql = """
        //         select e.*, d.department_name from employee e
        //         INNER JOIN department d ON e.department_id = d.department_id
        //         """;
        //
        // return r2dbc.withHandle(handle ->
        //         handle
        //                 .select(sql)
        //                 .mapRow(EMPLOYEE_ROWMAPPER)
        // )
        //         ;

        return Flux.empty();
    }

    @Override
    public Mono<Employee> getEmployee(final String lastName, final String firstName) {
        // final String sql = """
        //         select e.*, d.department_name from employee e
        //         INNER JOIN department d ON e.department_id = d.department_id
        //         where
        //         e.employee_lastname = ?
        //         and e.employee_firstname = ?
        //         """;
        //
        // return r2dbc.withHandle(handle ->
        //                 handle
        //                         .select(sql, lastName, firstName)
        //                         .mapRow(EMPLOYEE_ROWMAPPER)
        //         )
        //         .single()
        //         ;
        
        return Mono.empty();
    }
}
