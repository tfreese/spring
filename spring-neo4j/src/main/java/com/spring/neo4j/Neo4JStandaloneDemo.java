// Created: 30.06.2025
package com.spring.neo4j;

import java.nio.file.Path;
import java.time.Duration;

import com.spring.neo4j.logging.Slf4jLogProvider;
import com.spring.neo4j.model.RelTypes;
import org.neo4j.configuration.GraphDatabaseSettings;
import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <a href=https://neo4j.com/docs/java-reference/current/java-embedded/hello-world/>hello-world</a>
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
 * @author Thomas Freese
 */
public final class Neo4JStandaloneDemo {
    private static final Logger LOGGER = LoggerFactory.getLogger(Neo4JStandaloneDemo.class);

    public static void insertHelloWorld(final GraphDatabaseService graphDb) {
        final String firstNodeId;
        final String secondNodeId;

        try (Transaction tx = graphDb.beginTx()) {
            final Node firstNode = tx.createNode();
            firstNode.setProperty("message", "Hello, ");

            final Node secondNode = tx.createNode();
            secondNode.setProperty("message", "World!");

            final Relationship relationship = firstNode.createRelationshipTo(secondNode, RelTypes.KNOWS);
            relationship.setProperty("message", "brave Neo4j ");

            LOGGER.info("FirstNode ID: {}", firstNode.getElementId());
            LOGGER.info("Relationship ID: {}", relationship.getElementId());
            LOGGER.info("SecondNode ID: {}", secondNode.getElementId());

            firstNodeId = firstNode.getElementId();
            secondNodeId = secondNode.getElementId();

            LOGGER.info("{}{}{}", firstNode.getProperty("message"), relationship.getProperty("message"), secondNode.getProperty("message"));

            tx.commit();
        }

        deleteHelloWorld(graphDb, firstNodeId, secondNodeId);
    }

    public static void main(final String[] args) {
        final Path pathDB = Path.of(GraphDatabaseSettings.DEFAULT_DATABASE_NAME);

        final DatabaseManagementService managementService = new DatabaseManagementServiceBuilder(pathDB)
                // .loadPropertiesFromFile(Path.of(pathToConfig + "neo4j.conf"))
                .setUserLogProvider(new Slf4jLogProvider())
                .setConfig(GraphDatabaseSettings.udc_enabled, false) // Anonymous Usage Data reporting.
                .setConfig(GraphDatabaseSettings.transaction_timeout, Duration.ofSeconds(60L))
                .setConfig(GraphDatabaseSettings.preallocate_logical_logs, true)
                // .setConfig(BoltConnector.enabled, true)
                // .setConfig(BoltConnector.listen_address, new SocketAddress("localhost", 7687))
                // .setConfig(HttpConnector.enabled, true)
                .build();
        final GraphDatabaseService graphDb = managementService.database(GraphDatabaseSettings.DEFAULT_DATABASE_NAME);

        insertHelloWorld(graphDb);

        // Registers a shutdown hook for the Neo4j instance so that it shuts down nicely when the VM exits (even if you "Ctrl-C" the running application).
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("shutdown");
            managementService.shutdown();
        }));
    }

    private static void deleteHelloWorld(final GraphDatabaseService graphDb, final String firstNodeId, final String secondNodeId) {
        try (Transaction tx = graphDb.beginTx()) {
            final Node firstNode = tx.getNodeByElementId(firstNodeId);
            final Node secondNode = tx.getNodeByElementId(secondNodeId);

            firstNode.getSingleRelationship(RelTypes.KNOWS, Direction.OUTGOING).delete();

            firstNode.delete();
            secondNode.delete();

            tx.commit();
        }
    }

    private Neo4JStandaloneDemo() {
        super();
    }
}
