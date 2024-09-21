package de.freese.jdbc.dialect;

import org.springframework.boot.jdbc.DatabaseDriver;

/**
 * @author Thomas Freese
 */
class MySqlJdbcDialect implements JdbcDialect {
    @Override
    public String getLimitClause(final long limit, final long offset) {
        return String.format("LIMIT %s, %s", offset, limit);
    }

    @Override
    public String getValidationQuery() {
        return DatabaseDriver.MYSQL.getValidationQuery();
    }
}
