// Created: 30.06.2025
package com.spring.neo4j;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

import com.spring.neo4j.logging.Slf4jLogProvider;
import com.spring.neo4j.model.Person;
import com.spring.neo4j.repository.PersonRepository;
import org.neo4j.configuration.GraphDatabaseSettings;
import org.neo4j.configuration.connectors.BoltConnector;
import org.neo4j.configuration.helpers.SocketAddress;
import org.neo4j.cypherdsl.core.renderer.Configuration;
import org.neo4j.cypherdsl.core.renderer.Dialect;
import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder;
import org.neo4j.graphdb.GraphDatabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;

/**
 * @author Thomas Freese
 */
@SpringBootApplication
@EnableNeo4jRepositories
public class Neo4JApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(Neo4JApplication.class);

    public static void main(final String[] args) {
        SpringApplication.run(Neo4JApplication.class, args);
    }

    @Bean
    Configuration cypherDslConfiguration() {
        return Configuration.newConfig().withDialect(Dialect.NEO4J_5).build();
    }

    @Bean(destroyMethod = "shutdown")
    DatabaseManagementService databaseManagementService() {
        final Path pathDB = Path.of(System.getProperty("java.io.tmpdir"), GraphDatabaseSettings.DEFAULT_DATABASE_NAME);

        final DatabaseManagementService managementService = new DatabaseManagementServiceBuilder(pathDB)
                // .loadPropertiesFromFile(Path.of(pathToConfig + "neo4j.conf"))
                .setUserLogProvider(new Slf4jLogProvider())
                .setConfig(GraphDatabaseSettings.udc_enabled, false) // Anonymous Usage Data reporting.
                .setConfig(GraphDatabaseSettings.transaction_timeout, Duration.ofSeconds(60L))
                .setConfig(GraphDatabaseSettings.preallocate_logical_logs, true)
                .setConfig(BoltConnector.enabled, true) // For embedded.
                .setConfig(BoltConnector.listen_address, new SocketAddress("localhost", 7687)) // For embedded.
                // .setConfig(HttpConnector.enabled, true)
                .build();

        LOGGER.info("managementService created: {}", managementService.listDatabases());

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("shutdown");
            managementService.shutdown();
        }));

        return managementService;
    }

    @Bean
    @Order(1)
    CommandLineRunner demoNodes(final GraphDatabaseService graphDb) {
        return args -> Neo4JStandaloneDemo.insertHelloWorld(graphDb);
    }

    @Bean
    @Order(2)
    CommandLineRunner demoRepository(final PersonRepository personRepository) {
        return args -> {
            Person greg = new Person("Greg");
            Person roy = new Person("Roy");
            final Person craig = new Person("Craig");

            final List<String> team = List.of(greg.getName(), roy.getName(), craig.getName());

            // personRepository.deleteAll();

            if (personRepository.count() == 0) {
                LOGGER.info("Before linking up with Neo4j:");

                team.forEach(person -> LOGGER.info("\t{}", person));

                personRepository.save(greg);
                personRepository.save(roy);
                personRepository.save(craig);

                greg = personRepository.findByName(greg.getName());
                greg.worksWith(roy);
                greg.worksWith(craig);
                personRepository.save(greg);

                roy = personRepository.findByName(roy.getName());
                roy.worksWith(craig);

                // We already know that Roy works with Greg.
                personRepository.save(roy);
            }

            // We already know Craig works with Roy and Greg.

            LOGGER.info("Lookup each person by name:");
            team.forEach(name -> LOGGER.info("\t{}", personRepository.findByName(name)));

            final List<Person> teammates = personRepository.findByTeammatesName(craig.getName());
            LOGGER.info("The following have Craig as a teammate:");
            teammates.forEach(person -> LOGGER.info("\t{}", person.getName()));
        };
    }

    @Bean
    GraphDatabaseService graphDatabaseService(final DatabaseManagementService managementService) {
        final GraphDatabaseService graphDb = managementService.database(GraphDatabaseSettings.DEFAULT_DATABASE_NAME);

        LOGGER.info("graphDb with name '{}' is available: {}", graphDb.databaseName(), graphDb.isAvailable());

        return graphDb;
    }
}
