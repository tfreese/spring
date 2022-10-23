package de.freese.spring.reactive.repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;

import javax.sql.DataSource;

import de.freese.spring.reactive.model.Department;
import de.freese.spring.reactive.model.Employee;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Thomas Freese
 */
@Repository
@Profile("jdbc")
public class EmployeeRepositoryJdbc implements EmployeeRepository
{
    /**
     * @author Thomas Freese
     */
    private static class DepartmentRowMapper implements RowMapper<Department>
    {
        /**
         * @see org.springframework.jdbc.core.RowMapper#mapRow(java.sql.ResultSet, int)
         */
        @Override
        public Department mapRow(final ResultSet rs, final int rowNum) throws SQLException
        {
            Department department = new Department();
            department.setId(rs.getLong("department_id"));
            department.setName(rs.getString("department_name"));

            return department;
        }
    }

    /**
     * @author Thomas Freese
     */
    private static class EmployeeRowMapper implements RowMapper<Employee>
    {
        /**
         * @see org.springframework.jdbc.core.RowMapper#mapRow(java.sql.ResultSet, int)
         */
        @Override
        public Employee mapRow(final ResultSet rs, final int rowNum) throws SQLException
        {
            Employee employee = new Employee();
            employee.setId(rs.getLong("employee_id"));
            employee.setLastName(rs.getString("employee_lastname"));
            employee.setFirstName(rs.getString("employee_firstname"));
            employee.setDepartment(rs.getString("department_name"));

            return employee;
        }
    }

    private final JdbcTemplate jdbcTemplate;

    public EmployeeRepositoryJdbc(final DataSource dataSource)
    {
        super();

        this.jdbcTemplate = new JdbcTemplate(Objects.requireNonNull(dataSource, "dataSource required"));
    }

    /**
     * @see de.freese.spring.reactive.repository.EmployeeRepository#createNewEmployee(de.freese.spring.reactive.model.Employee)
     */
    @Override
    public Mono<Employee> createNewEmployee(final Employee newEmployee)
    {
        String sqlSelect = "SELECT department_id from department where department_name = ?";
        long departmentId = this.jdbcTemplate.queryForObject(sqlSelect, Long.class, newEmployee.getDepartment());

        String sqlInsert = "INSERT INTO employee (employee_lastname, employee_firstname, department_id) VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        this.jdbcTemplate.update(connection ->
        {
            PreparedStatement prepStmt = connection.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS);
            prepStmt.setString(1, newEmployee.getLastName());
            prepStmt.setString(2, newEmployee.getFirstName());
            prepStmt.setLong(3, departmentId);
            return prepStmt;
        }, keyHolder);

        long employeeId = keyHolder.getKey().longValue();
        newEmployee.setId(employeeId);

        return Mono.just(newEmployee);
    }

    /**
     * @see de.freese.spring.reactive.repository.EmployeeRepository#deleteEmployee(long)
     */
    @Override
    public Mono<Long> deleteEmployee(final long id)
    {
        String sql = "DELETE FROM employee WHERE employee_id = ?";

        long affectedRows = this.jdbcTemplate.update(sql, id);

        return Mono.just(affectedRows);
    }

    /**
     * @see de.freese.spring.reactive.repository.EmployeeRepository#getAllDepartments()
     */
    @Override
    public Flux<Department> getAllDepartments()
    {
        String sql = "select * from department";

        List<Department> result = this.jdbcTemplate.query(sql, new DepartmentRowMapper());

        return Flux.fromIterable(result);
    }

    /**
     * @see de.freese.spring.reactive.repository.EmployeeRepository#getAllEmployees()
     */
    @Override
    public Flux<Employee> getAllEmployees()
    {
        String sql = """
                select e.*, d.department_name
                from employee e
                INNER JOIN department d ON d.department_id = e.department_id
                """;

        List<Employee> result = this.jdbcTemplate.query(sql, new EmployeeRowMapper());

        return Flux.fromIterable(result);
    }

    /**
     * @see de.freese.spring.reactive.repository.EmployeeRepository#getEmployee(java.lang.String, java.lang.String)
     */
    @Override
    public Mono<Employee> getEmployee(final String lastName, final String firstName)
    {
        String sql = """
                select e.*, d.department_name
                from employee e
                INNER JOIN department d ON d.department_id = e.department_id
                where
                e.employee_lastname = ?
                and e.employee_firstname = ?
                """;

        Employee result = this.jdbcTemplate.queryForObject(sql, new EmployeeRowMapper(), lastName, firstName);

        return Mono.just(result);
    }
}
