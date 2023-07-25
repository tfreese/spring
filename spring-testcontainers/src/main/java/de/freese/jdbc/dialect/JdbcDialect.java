package de.freese.jdbc.dialect;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import javax.sql.DataSource;

/**
 * @author Thomas Freese
 */
public interface JdbcDialect {
    static JdbcDialect from(DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            return from(connection);
        }
    }

    static JdbcDialect from(Connection connection) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();

        String product = metaData.getDatabaseProductName();

        if ("h2".equalsIgnoreCase(product)) {
            return new H2JdbcDialect();
        }
        else if ("hsql".equalsIgnoreCase(product)) {
            return new HsqlDbJdbcDialect();
        }
        else if ("derby".equalsIgnoreCase(product)) {
            return new DerbyJdbcDialect();
        }
        else if ("maria".equalsIgnoreCase(product)) {
            return new MariaDbJdbcDialect();
        }
        else if ("mysql".equalsIgnoreCase(product)) {
            return new MySqlJdbcDialect();
        }
        else if ("postgres".equalsIgnoreCase(product)) {
            return new PostgresJdbcDialect();
        }
        else if ("oracle".equalsIgnoreCase(product)) {
            return new OracleJdbcDialect();
        }

        throw new IllegalArgumentException("unsupported database: " + product);
    }

    default String createSequence(String name) {
        return "CREATE SEQUENCE %s start with 1 increment by 1".formatted(name);
    }

    default String dropSequence(String name) {
        return "DROP SEQUENCE %s".formatted(name);
    }

    /**
     * As Standalone-Query: select current value for SEQUENCE
     */
    default String getSelectSequenceCurrentValString(String name) {
        return "select current value for %s".formatted(name);
    }

    /**
     * As Standalone-Query: select next value for SEQUENCE
     */
    default String getSelectSequenceNextValString(String name) {
        return "select next value for %s".formatted(name);
    }

    /**
     * Example: current value for SEQUENCE
     */
    default String getSequenceCurrentValString(String name) {
        return "current value for %s".formatted(name);
    }

    /**
     * Example: next value for SEQUENCE
     */
    default String getSequenceNextValString(String name) {
        return "next value for %s".formatted(name);
    }
}
