package de.freese.jdbc.dialect;

import org.springframework.boot.jdbc.DatabaseDriver;

/**
 * @author Thomas Freese
 */
class PostgresJdbcDialect implements JdbcDialect {
    @Override
    public String dropSequence(final String name) {
        // CASCADE löscht auch alle referenzierenden Objekte.
        // RESTRICT löscht die Sequence nur, wenn es keine weiteren Abhängigkeiten gibt.
        return "DROP SEQUENCE %s RESTRICT".formatted(name);
    }

    @Override
    public String getLimitClause(final long limit, final long offset) {
        return String.format("LIMIT %d OFFSET %d", limit, offset);
    }

    @Override
    public String getSelectSequenceNextValString(final String name) {
        return "select nextval('%s')".formatted(name);
    }

    @Override
    public String getSequenceNextValString(final String name) {
        return "nextval('%s')".formatted(name);
    }

    @Override
    public String getValidationQuery() {
        return DatabaseDriver.POSTGRESQL.getValidationQuery();
    }
}
