package de.freese.spring.testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.stream.IntStream;

import javax.sql.DataSource;

import jakarta.annotation.Resource;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import de.freese.jdbc.dialect.JdbcDialect;
import de.freese.spring.testcontainers.model.Person;
import de.freese.spring.testcontainers.service.PersonService;

/**
 * @author Thomas Freese
 */
@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
abstract class AbstractTest {
    @Resource
    private DataSource dataSource;
    @Resource
    private JdbcDialect jdbcDialect;
    @Resource
    private PersonService personService;

    @AfterEach
    void afterEach() throws Exception {
        try (Connection connection = getDataSource().getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE person");
            statement.execute(jdbcDialect.dropSequence("person_seq"));
        }
    }

    @BeforeEach
    void beforeEach() throws Exception {
        final ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("db-schema.sql"));
        populator.execute(getDataSource());
    }

    //    @Sql(scripts = "classpath:db-schema.sql")
    @Test
    void testGetAll() {
        // @formatter:off
        final List<Person> personsToSave = IntStream.rangeClosed(1, 3)
                .mapToObj(i -> {
                    final Person person = new Person();
                    person.setName("Name-" + i);

                    return person;
                })
                .toList()
                ;
        // @formatter:on

        personService.saveAll(personsToSave);

        final List<Person> persons = personService.getAll();

        assertNotNull(persons);
        assertEquals(3, persons.size());

        assertEquals(1, persons.getFirst().getId());
        assertEquals("Name-1", persons.getFirst().getName());

        assertEquals(2, persons.get(1).getId());
        assertEquals("Name-2", persons.get(1).getName());

        assertEquals(3, persons.get(2).getId());
        assertEquals("Name-3", persons.get(2).getName());
    }

    //    @Sql(scripts = "classpath:db-schema.sql")
    @Test
    void testSave() {
        final Person person = new Person();
        person.setName("Name");

        personService.save(person);

        final List<Person> persons = personService.getAll();

        assertNotNull(persons);
        assertEquals(1, persons.size());

        assertEquals(1, persons.getFirst().getId());
        assertEquals("Name", persons.getFirst().getName());
    }

    //    @Sql(scripts = "classpath:db-schema.sql")
    @Test
    void testSaveAllWithException() {
        // @formatter:off
        final List<Person> personsToSave = IntStream.rangeClosed(1, 3)
                .mapToObj(i -> {
                    final Person person = new Person();
                    person.setName("Name-" + i);

                    return person;
                })
                .toList()
                ;
        // @formatter:on

        final Exception exception = assertThrows(RuntimeException.class, () -> personService.saveAllWithException(personsToSave));

        assertEquals("saveAllWithException", exception.getMessage());

        final List<Person> persons = personService.getAll();

        assertNotNull(persons);
        assertEquals(0, persons.size());
    }

    protected DataSource getDataSource() {
        return this.dataSource;
    }
}
