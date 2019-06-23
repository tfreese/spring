/**
 *
 */
package de.freese.spring.reactive.repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import de.freese.spring.reactive.model.Department;
import de.freese.spring.reactive.model.Employee;
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
         * Erstellt ein neues {@link DepartmentRowMapper} Object.
         */
        public DepartmentRowMapper()
        {
            super();
        }

        /**
         * @see org.springframework.jdbc.core.RowMapper#mapRow(java.sql.ResultSet, int)
         */
        @Override
        public Department mapRow(final ResultSet rs, final int rowNum) throws SQLException
        {
            Department department = new Department();
            department.setId(rs.getInt("department_id"));
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
         * Erstellt ein neues {@link EmployeeRowMapper} Object.
         */
        public EmployeeRowMapper()
        {
            super();
        }

        /**
         * @see org.springframework.jdbc.core.RowMapper#mapRow(java.sql.ResultSet, int)
         */
        @Override
        public Employee mapRow(final ResultSet rs, final int rowNum) throws SQLException
        {
            Employee employee = new Employee();
            employee.setId(rs.getInt("employee_id"));
            employee.setDepartment(rs.getString("department_name"));
            employee.setFirstName(rs.getString("employee_firstname"));
            employee.setLastName(rs.getString("employee_lastname"));

            return employee;
        }
    }

    /**
     *
     */
    private final JdbcTemplate jdbcTemplate;

    /**
     * Erstellt ein neues {@link EmployeeRepositoryJdbc} Object.
     *
     * @param dataSource {@link DataSource}
     */
    public EmployeeRepositoryJdbc(final DataSource dataSource)
    {
        super();

        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /**
     * @see de.freese.spring.reactive.repository.EmployeeRepository#createNewEmployee(reactor.core.publisher.Mono)
     */
    @Override
    public Mono<Employee> createNewEmployee(final Mono<Employee> employeeMono)
    {
        final Employee employee;
        //
        employee = employeeMono.block();
        // employee = employeeMono.blockOptional().orElse(null);
        // final AtomicReference<Employee> employeeReference = new AtomicReference<>();
        // employeeMono.subscribe(employeeReference::set);
        // employee = employeeReference.get();

        // return employeeMono.map(employee -> {
        LoggerFactory.getLogger(EmployeeRepository.class).info(employee.toString());

        StringBuilder sqlSelect = new StringBuilder();
        sqlSelect.append("SELECT department_id from department where department_name = ?");

        int departmentId = this.jdbcTemplate.queryForObject(sqlSelect.toString(), Integer.class, employee.getDepartment());

        final StringBuilder sqlInsert = new StringBuilder();
        sqlInsert.append("INSERT INTO employee (employee_firstname, employee_lastname, department_id) VALUES (?, ?, ?)");

        KeyHolder keyHolder = new GeneratedKeyHolder();

        this.jdbcTemplate.update(connection -> {
            PreparedStatement prepStmt = connection.prepareStatement(sqlInsert.toString());
            prepStmt.setString(1, employee.getFirstName());
            prepStmt.setString(2, employee.getLastName());
            prepStmt.setInt(3, departmentId);
            return prepStmt;
        }, keyHolder);

        int employeeId = keyHolder.getKey().intValue();
        employee.setId(employeeId);

        // return employee;
        // });

        return Mono.just(employee);
    }

    /**
     * @see de.freese.spring.reactive.repository.EmployeeRepository#deleteEmployee(long)
     */
    @Override
    public Mono<Void> deleteEmployee(final long id)
    {
        StringBuilder sql = new StringBuilder();
        sql.append("DELETE FROM employee WHERE employee_id = ?");

        int affectedRows = this.jdbcTemplate.update(sql.toString(), id);

        return Flux.just(affectedRows).then();
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
        StringBuilder sql = new StringBuilder("select e.*, d.department_name from employee e");
        sql.append(" INNER JOIN department d ON e.department_id = d.department_id");

        List<Employee> result = this.jdbcTemplate.query(sql.toString(), new EmployeeRowMapper());

        return Flux.fromIterable(result);
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

        Employee result = this.jdbcTemplate.queryForObject(sql.toString(), new EmployeeRowMapper(), lastName, firstName);

        return Mono.just(result);
    }
}
