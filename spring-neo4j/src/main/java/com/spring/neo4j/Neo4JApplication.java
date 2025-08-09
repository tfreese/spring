// Created: 30.06.2025
package com.spring.neo4j;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

import com.spring.neo4j.logging.Slf4jLogProvider;
import com.spring.neo4j.model.MyLabels;
import com.spring.neo4j.model.MyRelationshipTypes;
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
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.data.neo4j.core.Neo4jTemplate;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * <a href=https://neo4j.com/docs/java-reference/current/java-embedded/hello-world>hello-world</a>
 *
 * <pre>{@code
 * // Embedded
 * implementation("org.neo4j:neo4j") {</br>
 *     exclude(module: "neo4j-slf4j-provider")</br>
 * }
 *
 * // Not embedded!
 * implementation("org.neo4j.driver:neo4j-java-driver")
 *
 * // Spring with embedded
 * implementation("org.neo4j:neo4j")
 * implementation("org.springframework.boot:spring-boot-starter-data-neo4j")
 * }</pre>
 *
 * <a href="https://neo4j.com/docs/cypher-manual/current/queries/basic/">basic queries</a>
 * <a href="https://neo4j.com/docs/cypher-cheat-sheet">cheat sheet</a>
 *
 * @author Thomas Freese
 */
@SpringBootApplication
@EnableNeo4jRepositories
@EnableTransactionManagement
public class Neo4JApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(Neo4JApplication.class);

    static void main(final String[] args) {
        SpringApplication.run(Neo4JApplication.class, args);

        System.exit(0);
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
                .setConfig(BoltConnector.enabled, true) // For embedded with spring.
                .setConfig(BoltConnector.listen_address, new SocketAddress("localhost", 7687)) // For embedded with spring.
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
    @Order(10)
    CommandLineRunner deleteAll(final GraphDatabaseService graphDb) {
        return args -> {
            try (Transaction tx = graphDb.beginTx()) {
                // final Node firstNode = tx.getNodeByElementId(firstNodeId);
                // final Node secondNode = tx.getNodeByElementId(secondNodeId);
                //
                // Relationships must be deleted before.
                // // relationship.delete();
                // firstNode.getSingleRelationship(RelTypes.KNOWS, Direction.OUTGOING).delete();
                //
                // firstNode.delete();
                // secondNode.delete();

                // Delete all without Indexes and Constraints.
                final Result result = tx.execute("MATCH (n) DETACH DELETE n");
                LOGGER.info("{}", result.getQueryStatistics());

                // Delete a large amount of Data without Indexes and Constraints.
                // final String query = """
                //         MATCH (n)
                //         CALL (n) {
                //             DETACH DELETE n
                //         } IN TRANSACTIONS
                //         """;
                // graphDb.executeTransactionally(query);

                // Delete all Data with Indexes and Constraints.
                // graphDb.executeTransactionally("CREATE OR REPLACE DATABASE " + GraphDatabaseSettings.DEFAULT_DATABASE_NAME);

                tx.commit();
            }
        };
    }

    @Bean
    @Order(1)
    CommandLineRunner demoNodes(final GraphDatabaseService graphDb) {
        return args -> {
            try (Transaction tx = graphDb.beginTx()) {
                final Node firstNode = tx.createNode();
                firstNode.setProperty("message", "Hello, ");

                final Node secondNode = tx.createNode();
                secondNode.setProperty("message", "World!");

                final Relationship relationship = firstNode.createRelationshipTo(secondNode, MyRelationshipTypes.KNOWS);
                relationship.setProperty("message", "brave Neo4j ");

                LOGGER.info("FirstNode ID: {}", firstNode.getElementId());
                LOGGER.info("Relationship ID: {}", relationship.getElementId());
                LOGGER.info("SecondNode ID: {}", secondNode.getElementId());

                LOGGER.info("{}{}{}", firstNode.getProperty("message"), relationship.getProperty("message"), secondNode.getProperty("message"));

                tx.commit();
            }
        };
    }

    @Bean
    @Order(3)
    CommandLineRunner demoQueries(final GraphDatabaseService graphDb) {
        return args -> {
            try (Transaction tx = graphDb.beginTx();
                 ResourceIterator<Node> result = tx.findNodes(MyLabels.PERSON)) {
                // ResourceIterator<Node> result = tx.findNodes(MyLabels.PERSON,"PROPERTY","VALUE");
                LOGGER.info("All Persons by API:");
                result.forEachRemaining(node -> LOGGER.info("{}", node.getAllProperties()));
            }

            try (Transaction tx = graphDb.beginTx();
                 // Result result = tx.execute("MATCH (n) RETURN n")) {
                 Result result = tx.execute("MATCH (p:PERSON) RETURN distinct p.message AS name, labels(p) ORDER BY name asc")) {
                LOGGER.info("All Persons by Query:");

                LOGGER.info(result.resultAsString());

                result.forEachRemaining(row -> {
                            for (String key : result.columns()) {
                                final Object value = row.get(key);

                                if (value instanceof final Node node) {
                                    LOGGER.info("Node: {} = {}", key, node.getAllProperties());
                                }
                                else if (value instanceof final Relationship rs) {
                                    LOGGER.info("Relationship: {} = {}", key, rs.getAllProperties());
                                }
                                else {
                                    LOGGER.info("{} = {}", key, row.get(key));
                                }
                            }
                        }
                );
            }
        };
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
    @Order(4)
    CommandLineRunner demoTemplate(final Neo4jTemplate neo4jTemplate, final PlatformTransactionManager txManager) {
        return args -> {
            LOGGER.info("All Persons by Neo4jTemplate:");
            neo4jTemplate.findAll(Person.class).forEach(p -> LOGGER.info("{}", p));
            // neo4jTemplate.findAll("MATCH (p:PERSON) RETURN p", Person.class).forEach(p -> LOGGER.info("{}", p));
        };
    }

    @Bean
    GraphDatabaseService graphDatabaseService(final DatabaseManagementService managementService) {
        final GraphDatabaseService graphDb = managementService.database(GraphDatabaseSettings.DEFAULT_DATABASE_NAME);

        LOGGER.info("graphDb with name '{}' is available: {}", graphDb.databaseName(), graphDb.isAvailable());

        return graphDb;
    }
}
