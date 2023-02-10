package de.freese.jdbc.dialect;

/**
 * @author Thomas Freese
 */
class DerbyJdbcDialect implements JdbcDialect {
    @Override
    public String dropSequence(final String name) {
        return "DROP SEQUENCE %s RESTRICT".formatted(name);
    }

    @Override
    public String getSelectSequenceNextValString(final String name) {
        return "values next value for %s".formatted(name);
    }
}
