package de.freese.jdbc.dialect;

import org.springframework.boot.jdbc.DatabaseDriver;

/**
 * @author Thomas Freese
 */
class HsqlDbJdbcDialect implements JdbcDialect {
    @Override
    public String getLimitClause(final long limit, final long offset) {
        return String.format("OFFSET %d LIMIT %d", offset, limit);
    }

    @Override
    public String getSelectSequenceNextValString(final String name) {
        return "call next value for %s".formatted(name);
    }

    @Override
    public String getValidationQuery() {
        return DatabaseDriver.HSQLDB.getValidationQuery();
    }
}
