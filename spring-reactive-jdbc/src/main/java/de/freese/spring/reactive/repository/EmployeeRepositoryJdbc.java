package de.freese.spring.reactive.repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;

import javax.sql.DataSource;

import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import de.freese.spring.reactive.model.Department;
import de.freese.spring.reactive.model.Employee;

/**
 * @author Thomas Freese
 */
@Repository
@Profile("jdbc")
public class EmployeeRepositoryJdbc implements EmployeeRepository {
    /**
     * @author Thomas Freese
     */
    private static final class DepartmentRowMapper implements RowMapper<Department> {
        @Override
        public Department mapRow(final ResultSet rs, final int rowNum) throws SQLException {
            final Department department = new Department();
            department.setId(rs.getLong("department_id"));
            department.setName(rs.getString("department_name"));

            return department;
        }
    }

    /**
     * @author Thomas Freese
     */
    private static final class EmployeeRowMapper implements RowMapper<Employee> {
        @Override
        public Employee mapRow(final ResultSet rs, final int rowNum) throws SQLException {
            final Employee employee = new Employee();
            employee.setId(rs.getLong("employee_id"));
            employee.setLastName(rs.getString("employee_lastname"));
            employee.setFirstName(rs.getString("employee_firstname"));
            employee.setDepartment(rs.getString("department_name"));

            return employee;
        }
    }

    private final JdbcTemplate jdbcTemplate;

    public EmployeeRepositoryJdbc(final DataSource dataSource) {
        super();

        this.jdbcTemplate = new JdbcTemplate(Objects.requireNonNull(dataSource, "dataSource required"));
    }

    @Override
    public Mono<Employee> createNewEmployee(final Employee newEmployee) {
        final String sqlSelect = "SELECT department_id from department where department_name = ?";
        final long departmentId = this.jdbcTemplate.queryForObject(sqlSelect, Long.class, newEmployee.getDepartment());

        final String sqlInsert = "INSERT INTO employee (employee_lastname, employee_firstname, department_id) VALUES (?, ?, ?)";
        final KeyHolder keyHolder = new GeneratedKeyHolder();

        this.jdbcTemplate.update(connection -> {
            final PreparedStatement prepStmt = connection.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS);
            prepStmt.setString(1, newEmployee.getLastName());
            prepStmt.setString(2, newEmployee.getFirstName());
            prepStmt.setLong(3, departmentId);
            return prepStmt;
        }, keyHolder);

        final long employeeId = keyHolder.getKey().longValue();
        newEmployee.setId(employeeId);

        return Mono.just(newEmployee);
    }

    @Override
    public Mono<Long> deleteEmployee(final long id) {
        final String sql = "DELETE FROM employee WHERE employee_id = ?";

        final long affectedRows = this.jdbcTemplate.update(sql, id);

        return Mono.just(affectedRows);
    }

    @Override
    public Flux<Department> getAllDepartments() {
        final String sql = "select * from department";

        final List<Department> result = this.jdbcTemplate.query(sql, new DepartmentRowMapper());

        return Flux.fromIterable(result);
    }

    @Override
    public Flux<Employee> getAllEmployees() {
        final String sql = """
                select e.*, d.department_name
                from employee e
                INNER JOIN department d ON d.department_id = e.department_id
                """;

        final List<Employee> result = this.jdbcTemplate.query(sql, new EmployeeRowMapper());

        return Flux.fromIterable(result);
    }

    @Override
    public Mono<Employee> getEmployee(final String lastName, final String firstName) {
        final String sql = """
                select e.*, d.department_name
                from employee e
                INNER JOIN department d ON d.department_id = e.department_id
                where
                e.employee_lastname = ?
                and e.employee_firstname = ?
                """;

        final Employee result = this.jdbcTemplate.queryForObject(sql, new EmployeeRowMapper(), lastName, firstName);

        return Mono.just(result);
    }
}
