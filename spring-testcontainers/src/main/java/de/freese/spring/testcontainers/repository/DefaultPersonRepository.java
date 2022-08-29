package de.freese.spring.testcontainers.repository;

import java.util.List;

import javax.sql.DataSource;

import de.freese.spring.testcontainers.model.Person;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * @author Thomas Freese
 */
@Repository
public class DefaultPersonRepository implements PersonRepository
{
    private final JdbcTemplate jdbcTemplate;

    public DefaultPersonRepository(DataSource dataSource)
    {
        super();

        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public void save(final Person person)
    {
        long id = this.jdbcTemplate.queryForObject("select next value for person_seq", Long.class);

        this.jdbcTemplate.update("insert into person (id, name) values (? , ?)", id, person.getName());
    }

    @Override
    public List<Person> getAllOrderedById()
    {
        return this.jdbcTemplate.query("select * from person order by id asc", (resultSet, rowNum) ->
        {
            Person person = new Person();
            person.setId(resultSet.getLong("ID"));
            person.setName(resultSet.getString("NAME"));

            return person;
        });
    }
}
