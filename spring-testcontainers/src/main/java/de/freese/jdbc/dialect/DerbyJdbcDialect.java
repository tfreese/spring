package de.freese.jdbc.dialect;

import org.springframework.boot.jdbc.DatabaseDriver;

/**
 * @author Thomas Freese
 */
class DerbyJdbcDialect implements JdbcDialect {
    @Override
    public String dropSequence(final String name) {
        return "DROP SEQUENCE %s RESTRICT".formatted(name);
    }

    @Override
    public String getLimitClause(final long limit, final long offset) {
        throw new UnsupportedOperationException("limit not supported");
    }

    @Override
    public String getSelectSequenceNextValString(final String name) {
        return "values next value for %s".formatted(name);
    }

    @Override
    public String getValidationQuery() {
        return DatabaseDriver.DERBY.getValidationQuery();
    }
}
