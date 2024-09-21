package de.freese.jdbc.dialect;

import org.springframework.boot.jdbc.DatabaseDriver;

/**
 * @author Thomas Freese
 */
class H2JdbcDialect implements JdbcDialect {
    @Override
    public String getLimitClause(final long limit, final long offset) {
        return String.format("OFFSET %d ROWS FETCH FIRST %d ROWS ONLY", offset, limit);
    }

    @Override
    public String getValidationQuery() {
        return DatabaseDriver.H2.getValidationQuery();
    }
}
