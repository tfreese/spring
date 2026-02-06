package de.spring.ai.config;

import java.sql.SQLException;

import javax.sql.DataSource;

import de.spring.ai.tools.sql.DatabaseMetadataAdvisor;
import de.spring.ai.tools.sql.RunSqlQueryTool;
import de.spring.ai.tools.sql.metadata.DbMetaDataHelper;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.simple.JdbcClient;
import tools.jackson.databind.json.JsonMapper;

/**
 * @author Thomas Freese
 */
@Configuration
public class DatabaseControllerConfig {
    @Bean
    DatabaseMetadataAdvisor databaseMetadataAdvisor(final DbMetaDataHelper dbMetaDataHelper) throws SQLException {
        return new DatabaseMetadataAdvisor(dbMetaDataHelper);
    }

    @Bean
    @DependsOnDatabaseInitialization
    DbMetaDataHelper dbMetaDataHelper(final DataSource dataSource, final JsonMapper jsonMapper) {
        return new DbMetaDataHelper(dataSource, jsonMapper);
    }

    @Bean
    @Tool(name = "runSqlQuery", description = "Query database using SQL")
    RunSqlQueryTool runSqlQuery(final JdbcClient jdbcClient) {
        return new RunSqlQueryTool(jdbcClient);
    }
}
