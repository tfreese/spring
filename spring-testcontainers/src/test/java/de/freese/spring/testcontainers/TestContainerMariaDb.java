package de.freese.spring.testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.stream.IntStream;

import javax.annotation.Resource;
import javax.sql.DataSource;

import de.freese.spring.testcontainers.model.Person;
import de.freese.spring.testcontainers.service.PersonService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * @author Thomas Freese
 */
@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
@Testcontainers
class TestContainerMariaDb
{
    /**
     *
     */
    @Resource
    private PersonService personService;

    /**
     *
     */
    @Resource
    private DataSource dataSource;

    /**
     * will be shared between test methods<br />
     * DockerImageName.parse(MariaDBContainer.NAME)
     * mariadb:latest<br />
     */
    @Container
    private static final MariaDBContainer MARIADB_CONTAINER = new MariaDBContainer("mariadb:latest");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry)
    {
        registry.add("spring.datasource.driver-class-name", MARIADB_CONTAINER::getDriverClassName);
        registry.add("spring.datasource.url", MARIADB_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", MARIADB_CONTAINER::getUsername);
        registry.add("spring.datasource.password", MARIADB_CONTAINER::getPassword);

        registry.add("spring.datasource.hikari.pool-name", () -> "Hikari-" + MARIADB_CONTAINER.getDockerImageName());
    }

    //    /**
    //     * will be started before and stopped after each test method<br />
    //     * DockerImageName.parse(MariaDBContainer.NAME)
    //     * mariadb:latest
    //     */
    //    @Container
    //    private MariaDBContainer mariaDBContainer = new MariaDBContainer(DockerImageName.parse(MariaDBContainer.NAME));
    //    //            .withDatabaseName("foo")
    //    //            .withUsername("foo")
    //    //            .withPassword("secret");

    //    @Bean
    //    DataSource dataSource()
    //    {
    //        HikariConfig config = new HikariConfig();
    //        config.setDriverClassName(mariaDBContainer.getDriverClassName());
    //        config.setJdbcUrl(mariaDBContainer.getJdbcUrl());
    //        config.setUsername(mariaDBContainer.getUsername());
    //        config.setPassword(mariaDBContainer.getPassword());
    //
    //        return new HikariDataSource(config);
    //    }

    @AfterEach
    void afterEach() throws SQLException
    {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement())
        {
            statement.execute("DROP TABLE person");
            statement.execute("DROP SEQUENCE person_seq");
        }
    }

    /**
     *
     */
    @Sql(scripts = "classpath:db-schema.sql")
    @Test
    void testSave()
    {
        Person person = new Person();
        person.setName("Name");

        personService.save(person);

        List<Person> persons = personService.getAll();

        assertNotNull(persons);
        assertEquals(1, persons.size());

        assertEquals(1, persons.get(0).getId());
        assertEquals("Name", persons.get(0).getName());
    }

    /**
     *
     */
    @Sql(scripts = "classpath:db-schema.sql")
    @Test
    void testGetAll()
    {
        // @formatter:off
        final List<Person> personsToSave = IntStream.rangeClosed(1, 3)
                .mapToObj(i -> {
                    Person person = new Person();
                    person.setName("Name-" + i);

                    return person;
                })
                .toList()
                ;
        // @formatter:on

        personService.saveAll(personsToSave);

        List<Person> persons = personService.getAll();

        assertNotNull(persons);
        assertEquals(3, persons.size());

        assertEquals(1, persons.get(0).getId());
        assertEquals("Name-1", persons.get(0).getName());

        assertEquals(2, persons.get(1).getId());
        assertEquals("Name-2", persons.get(1).getName());

        assertEquals(3, persons.get(2).getId());
        assertEquals("Name-3", persons.get(2).getName());
    }

    /**
     *
     */
    @Sql(scripts = "classpath:db-schema.sql")
    @Test
    void testSaveAllWithException()
    {
        // @formatter:off
        final List<Person> personsToSave = IntStream.rangeClosed(1, 3)
                .mapToObj(i -> {
                    Person person = new Person();
                    person.setName("Name-" + i);

                    return person;
                })
                .toList()
                ;
        // @formatter:on

        Exception exception = assertThrows(RuntimeException.class, () ->
        {
            personService.saveAllWithException(personsToSave);
        });

        assertEquals("saveAllWithException", exception.getMessage());

        List<Person> persons = personService.getAll();

        assertNotNull(persons);
        assertEquals(0, persons.size());
    }
}
