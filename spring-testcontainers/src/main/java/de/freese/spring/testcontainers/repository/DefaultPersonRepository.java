package de.freese.spring.testcontainers.repository;

import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import de.freese.jdbc.dialect.JdbcDialect;
import de.freese.spring.testcontainers.model.Person;

/**
 * @author Thomas Freese
 */
@Repository
public class DefaultPersonRepository implements PersonRepository {

    private final JdbcDialect jdbcDialect;

    private final JdbcTemplate jdbcTemplate;

    public DefaultPersonRepository(final DataSource dataSource, final JdbcDialect jdbcDialect) {
        super();

        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.jdbcDialect = jdbcDialect;
    }

    @Override
    public List<Person> getAllOrderedById() {
        return this.jdbcTemplate.query("select * from person order by id asc", (resultSet, rowNum) -> {
            Person person = new Person();
            person.setId(resultSet.getLong("ID"));
            person.setName(resultSet.getString("NAME"));

            return person;
        });
    }

    @Override
    public void save(final Person person) {
        long id = this.jdbcTemplate.queryForObject(jdbcDialect.getSelectSequenceNextValString("person_seq"), Long.class);

        this.jdbcTemplate.update("insert into person (id, name) values (? , ?)", id, person.getName());
    }

    @Override
    public void saveAll(final List<Person> persons) {
        String sql = "insert into person (id, name) values (%s , ?)".formatted(jdbcDialect.getSequenceNextValString("person_seq"));

        this.jdbcTemplate.batchUpdate(sql, persons, 10, (ps, person) -> ps.setString(1, person.getName()));
    }
}
