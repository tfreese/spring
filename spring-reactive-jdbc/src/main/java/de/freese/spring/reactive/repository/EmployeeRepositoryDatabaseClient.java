package de.freese.spring.reactive.repository;

import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;

import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import org.springframework.context.annotation.Profile;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import de.freese.spring.reactive.model.Department;
import de.freese.spring.reactive.model.Employee;

/**
 * @author Thomas Freese
 */
@Repository
@Profile("r2dbc")
public class EmployeeRepositoryDatabaseClient implements EmployeeRepository {
    private static final BiFunction<Row, RowMetadata, Department> DEPARTMENT_ROWMAPPER = (row, rowMetadata) -> {
        final Department department = new Department();
        department.setId(row.get("department_id", Long.class));
        department.setName(row.get("department_name", String.class));

        return department;
    };

    private static final BiFunction<Row, RowMetadata, Employee> EMPLOYEE_ROWMAPPER = (row, rowMetadata) -> {
        final Employee employee = new Employee();
        employee.setId(row.get("employee_id", Long.class));
        employee.setLastName(row.get("employee_lastname", String.class));
        employee.setFirstName(row.get("employee_firstname", String.class));
        employee.setDepartment(row.get("department_name", String.class));

        return employee;
    };

    private final DatabaseClient databaseClient;

    public EmployeeRepositoryDatabaseClient(final ConnectionFactory connectionFactory) {
        super();

        databaseClient = DatabaseClient.builder()
                .connectionFactory(Objects.requireNonNull(connectionFactory, "connectionFactory required"))
                .namedParameters(true)
                .build()
        ;
    }

    @Override
    public Mono<Employee> createNewEmployee(final Employee newEmployee) {
        // Das block() ist hier ein Problem, wenn das DAO in einem Reactive-Server läuft.
        // Meldung: java.lang.IllegalStateException: block()/blockFirst()/blockLast() are blocking, which is not supported

        // final AtomicInteger departmentId = new AtomicInteger(-1);
        //
        // databaseClient.sql("SELECT department_id from department where department_name = :name")
        //         .bind("name", newEmployee.getDepartment())
        //         .map((row, rowMetadata) -> row.get("department_id", Integer.class))
        //         .one()
        //         //.block()
        //         .subscribe(departmentId::set)
        // ;
        //
        // return databaseClient.sql("INSERT INTO employee (employee_lastname, employee_firstname, department_id) VALUES (:lastName, :firstName, :depId)")
        //         .filter((statement, executeFunction) -> statement.returnGeneratedValues("employee_id").execute())
        //         .bind("lastName", newEmployee.getLastName())
        //         .bind("firstName", newEmployee.getFirstName())
        //         .bind("depId", departmentId.get())
        //         .fetch()
        //         .first()
        //         .map(r -> {
        //             final long employeeId = (Long) r.get("employee_id");
        //             newEmployee.setId(employeeId);
        //             return newEmployee;
        //         })
        //         ;

        return databaseClient.sql("SELECT department_id from department where department_name = :name")
                .bind("name", newEmployee.getDepartment())
                //.map((row, rowMetadata) -> row.get("department_id", Long.class))
                .map(row -> row.get("department_id", Long.class))
                .one()
                .flatMap(depId ->
                        databaseClient.sql("INSERT INTO employee (employee_lastname, employee_firstname, department_id) VALUES (:lastName, :firstName, :depId)")
                                .filter((statement, executeFunction) -> statement.returnGeneratedValues("employee_id").execute())
                                .bind("lastName", newEmployee.getLastName())
                                .bind("firstName", newEmployee.getFirstName())
                                .bind("depId", depId)
                                .fetch()
                                .first()
                                .map(generatedValues -> {
                                    final long employeeId = (Long) generatedValues.get("employee_id");
                                    newEmployee.setId(employeeId);
                                    return newEmployee;
                                }))
                ;
    }

    @Override
    public Mono<Long> deleteEmployee(final long id) {
        return databaseClient.sql("DELETE FROM employee WHERE employee_id = :id").bind("id", id).fetch().rowsUpdated();
    }

    @Override
    public Flux<Department> getAllDepartments() {
        return databaseClient.sql("select * from department")
                //.filter((statement, executeFunction) -> statement.fetchSize(10).execute())
                .map(DEPARTMENT_ROWMAPPER)
                .all()
                ;
    }

    @Override
    public Flux<Employee> getAllEmployees() {
        final String sql = """
                select e.*, d.department_name
                from employee e
                INNER JOIN department d ON d.department_id = e.department_id
                """;

        return databaseClient.sql(sql)
                //.filter((statement, executeFunction) -> statement.fetchSize(10).execute())
                .map(EMPLOYEE_ROWMAPPER)
                .all()
                ;
    }

    @Override
    public Mono<Employee> getEmployee(final String lastName, final String firstName) {
        final String sql = """
                select e.*, d.department_name
                from employee e
                INNER JOIN department d ON d.department_id = e.department_id
                where
                e.employee_lastname = :lastName
                and e.employee_firstname = :firstName
                """;

        return databaseClient.sql(sql)
                .bind("lastName", lastName)
                .bind("firstName", firstName)
                .map(EMPLOYEE_ROWMAPPER)
                .one()
                ;
    }

    public Flux<Long> saveAll(final List<Department> data) {
        return databaseClient.inConnectionMany(connection -> {
            //            connection.createBatch()
            final var statement = connection.createStatement("INSERT INTO department (department_name) VALUES (:name)").returnGeneratedValues("department_id");

            for (var d : data) {
                statement.bind(0, d.getName()).add();
            }

            return Flux.from(statement.execute()).flatMap(result -> result.map((row, rowMetadata) -> row.get("department_id", Long.class)));
        });
    }
}
