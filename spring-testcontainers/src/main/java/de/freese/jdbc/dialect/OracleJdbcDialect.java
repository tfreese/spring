package de.freese.jdbc.dialect;

import org.springframework.boot.jdbc.DatabaseDriver;

/**
 * @author Thomas Freese
 */
class OracleJdbcDialect implements JdbcDialect {
    @Override
    public String getLimitClause(final long limit, final long offset) {
        return String.format("OFFSET %d ROWS FETCH FIRST %d ROWS ONLY", offset, limit);
    }

    @Override
    public String getSelectSequenceNextValString(final String name) {
        return "select %s.nextval from dual".formatted(name);
    }

    @Override
    public String getSequenceNextValString(final String name) {
        return "%s.nextval".formatted(name);
    }

    @Override
    public String getValidationQuery() {
        return DatabaseDriver.ORACLE.getValidationQuery();
    }
}
