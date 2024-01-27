package de.freese.jdbc.dialect;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import javax.sql.DataSource;

/**
 * @author Thomas Freese
 */
public interface JdbcDialect {
    static JdbcDialect from(final DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            return from(connection);
        }
    }

    static JdbcDialect from(final Connection connection) throws SQLException {
        final DatabaseMetaData metaData = connection.getMetaData();

        final String product = metaData.getDatabaseProductName().toLowerCase();

        if (product.contains("h2")) {
            return new H2JdbcDialect();
        }
        else if (product.contains("hsql")) {
            return new HsqlDbJdbcDialect();
        }
        else if (product.contains("derby")) {
            return new DerbyJdbcDialect();
        }
        else if (product.contains("maria")) {
            return new MariaDbJdbcDialect();
        }
        else if (product.contains("mysql")) {
            return new MySqlJdbcDialect();
        }
        else if (product.contains("postgres")) {
            return new PostgresJdbcDialect();
        }
        else if (product.contains("oracle")) {
            return new OracleJdbcDialect();
        }

        throw new IllegalArgumentException("unsupported database: " + product);
    }

    default String createSequence(final String name) {
        return "CREATE SEQUENCE %s start with 1 increment by 1".formatted(name);
    }

    default String dropSequence(final String name) {
        return "DROP SEQUENCE %s".formatted(name);
    }

    /**
     * As Standalone-Query: select current value for SEQUENCE
     */
    default String getSelectSequenceCurrentValString(final String name) {
        return "select current value for %s".formatted(name);
    }

    /**
     * As Standalone-Query: select next value for SEQUENCE
     */
    default String getSelectSequenceNextValString(final String name) {
        return "select next value for %s".formatted(name);
    }

    /**
     * Example: current value for SEQUENCE
     */
    default String getSequenceCurrentValString(final String name) {
        return "current value for %s".formatted(name);
    }

    /**
     * Example: next value for SEQUENCE
     */
    default String getSequenceNextValString(final String name) {
        return "next value for %s".formatted(name);
    }
}
