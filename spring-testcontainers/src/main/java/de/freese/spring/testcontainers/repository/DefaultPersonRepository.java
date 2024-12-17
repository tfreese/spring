package de.freese.spring.testcontainers.repository;

import java.util.List;
import java.util.Objects;

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

        this.jdbcTemplate = new JdbcTemplate(Objects.requireNonNull(dataSource, "dataSource required"));
        this.jdbcDialect = Objects.requireNonNull(jdbcDialect, "jdbcDialect required");
    }

    @Override
    public List<Person> getAllOrderedById() {
        return jdbcTemplate.query("select * from person order by id asc", (resultSet, rowNum) -> {
            final Person person = new Person();
            person.setId(resultSet.getLong("ID"));
            person.setName(resultSet.getString("NAME"));

            return person;
        });
    }

    @Override
    public void save(final Person person) {
        final String sqlSequence = jdbcDialect.getSelectSequenceNextValString("person_seq");
        final Long id = jdbcTemplate.queryForObject(sqlSequence, Long.class);

        Objects.requireNonNull(id, "id required");

        jdbcTemplate.update("insert into person (id, name) values (? , ?)", id, person.getName());
    }

    @Override
    public void saveAll(final List<Person> persons) {
        final String sqlSequence = jdbcDialect.getSequenceNextValString("person_seq");
        final String sql = "insert into person (id, name) values (%s , ?)".formatted(sqlSequence);

        jdbcTemplate.batchUpdate(sql, persons, 10, (ps, person) -> ps.setString(1, person.getName()));
    }
}
