package de.freese.spring.testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.stream.IntStream;

import javax.annotation.Resource;

import de.freese.spring.testcontainers.model.Person;
import de.freese.spring.testcontainers.service.PersonService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

/**
 * @author Thomas Freese
 */
@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD) // Jede Methode mit eigenem Context.
@Disabled("H2 Treiber entfernt für TestContainerMariaDb")
class TestH2Classic
{
    /**
     *
     */
    @Resource
    private PersonService personService;

    //    @BeforeEach
    //    public void beforeEach()
    //    {
    //        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
    //        populator.addScript(new ClassPathResource("db-schema.sql"));
    //        populator.execute(dataSource);
    //    }

    //    @AfterEach
    //    void afterEach() throws SQLException
    //    {
    //        try (Connection connection = dataSource.getConnection();
    //             Statement statement = connection.createStatement())
    //        {
    //            statement.execute("DROP TABLE person");
    //            statement.execute("DROP SEQUENCE person_seq");
    //        }
    //    }

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
