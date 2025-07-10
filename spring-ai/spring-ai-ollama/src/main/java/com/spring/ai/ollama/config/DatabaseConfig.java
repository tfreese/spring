package com.spring.ai.ollama.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@Profile("database")
public class DatabaseConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseConfig.class);

    @Bean
    @DependsOn({"dataSourceInitializer"})
    ChatMemoryRepository chatMemoryRepository(final DataSource dataSource, final PlatformTransactionManager txManager) {
        return JdbcChatMemoryRepository.builder()
                .dataSource(dataSource)
                .transactionManager(txManager)
                .build();
    }

    @Bean
    DataSourceInitializer dataSourceInitializer(final DataSource dataSource) throws IOException {
        final Resource resourceJdbcChatMemoryRepository = new ClassPathResource("/org/springframework/ai/chat/memory/repository/jdbc/schema-hsqldb.sql");

        final String sql = resourceJdbcChatMemoryRepository.getContentAsString(StandardCharsets.UTF_8)
                .replace("CREATE TABLE", "CREATE TABLE IF NOT EXISTS")
                .replace("CREATE INDEX", "CREATE INDEX IF NOT EXISTS")
                .replace("ADD CONSTRAINT", "ADD CONSTRAINT IF NOT EXISTS");

        final ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ByteArrayResource(sql.getBytes(StandardCharsets.UTF_8)));
        populator.addScript(new ClassPathResource("schema-document.sql"));
        // populator.execute(dataSource);

        final DataSourceInitializer dataSourceInitializer = new DataSourceInitializer();
        dataSourceInitializer.setDataSource(dataSource);
        dataSourceInitializer.setDatabasePopulator(populator);

        return dataSourceInitializer;
    }

    // @Bean
    // ChatMemoryRepository chatMemoryRepository(final Driver driver) {
    //     return new Neo4jChatMemoryRepository(Neo4jChatMemoryRepositoryConfig.builder()
    //             .withDriver(driver)
    //             .build());
    // }

    // @Bean
    // public DataSource getDataSource(@Value("${spring.datasource.username}") final String username,
    //                                 @Value("${spring.datasource.password}") final String password,
    //                                 final PasswordEncoder passwordEncoder) {
    //     final DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
    //     dataSourceBuilder.username(username);
    //     dataSourceBuilder.password(password);
    //     // dataSourceBuilder.password(passwordEncoder.decode(password));
    //
    //     return dataSourceBuilder.build();
    // }

    // @Bean
    // @DependsOn({"databaseManagementService"})
    // public Driver driver(@Value("${spring.neo4j.uri}") final String uri) {
    //     return GraphDatabase.driver(uri,
    //             AuthTokens.basic("", ""));
    // }

    // @Bean
    // VectorStore vectorStore(final EmbeddingModel embeddingModel, final JdbcTemplate jdbcTemplate) {
    //     return JdbcVectorStore.builder(embeddingModel).jdbcTemplate(jdbcTemplate).build();
    // }

    // @Bean(destroyMethod = "shutdown")
    // DatabaseManagementService databaseManagementService() {
    //     final Path pathDB = Path.of(System.getProperty("java.io.tmpdir"), GraphDatabaseSettings.DEFAULT_DATABASE_NAME);
    //
    //     final DatabaseManagementService managementService = new DatabaseManagementServiceBuilder(pathDB)
    //             // .loadPropertiesFromFile(Path.of(pathToConfig + "neo4j.conf"))
    //             // .setUserLogProvider(new Slf4jLogProvider())
    //             // .setUserLogProvider(new Slf4jLogProvider())
    //             .setConfig(GraphDatabaseSettings.udc_enabled, false) // Anonymous Usage Data reporting.
    //             .setConfig(GraphDatabaseSettings.transaction_timeout, Duration.ofSeconds(60L))
    //             .setConfig(GraphDatabaseSettings.preallocate_logical_logs, true)
    //             .setConfig(BoltConnector.enabled, true) // For embedded with spring.
    //             .setConfig(BoltConnector.listen_address, new SocketAddress("localhost", 7687)) // For embedded with spring.
    //             // .setConfig(HttpConnector.enabled, true)
    //             .build();
    //
    //     LOGGER.info("managementService created: {}", managementService.listDatabases());
    //
    //     Runtime.getRuntime().addShutdownHook(new Thread(() -> {
    //         LOGGER.info("shutdown");
    //         managementService.shutdown();
    //     }));
    //
    //     return managementService;
    // }

    // @Bean
    // @DependsOn({"databaseManagementService"})
    // public VectorStore vectorStore(final Driver driver, final EmbeddingModel embeddingModel) {
    //     return Neo4jVectorStore.builder(driver, embeddingModel)
    //             .databaseName("neo4j")                // Optional: defaults to "neo4j"
    //             .distanceType(Neo4jVectorStore.Neo4jDistanceType.COSINE) // Optional: defaults to COSINE
    //             .embeddingDimension(1536)                      // Optional: defaults to 1536
    //             .label("Document")                     // Optional: defaults to "Document"
    //             .embeddingProperty("embedding")        // Optional: defaults to "embedding"
    //             .indexName("custom-index")             // Optional: defaults to "spring-ai-document-index"
    //             .initializeSchema(true)                // Optional: defaults to false
    //             .batchingStrategy(new TokenCountBatchingStrategy()) // Optional: defaults to TokenCountBatchingStrategy
    //             .build();
    // }

    // @Bean
    // org.neo4j.cypherdsl.core.renderer.Configuration cypherDslConfiguration() {
    //     return org.neo4j.cypherdsl.core.renderer.Configuration.newConfig().withDialect(Dialect.NEO4J_5).build();
    // }

    // @Bean
    // GraphDatabaseService graphDatabaseService(final DatabaseManagementService managementService) {
    //     final GraphDatabaseService graphDb = managementService.database(GraphDatabaseSettings.DEFAULT_DATABASE_NAME);
    //
    //     LOGGER.info("graphDb with name '{}' is available: {}", graphDb.databaseName(), graphDb.isAvailable());
    //
    //     return graphDb;
    // }
}
