package de.freese.jdbc.dialect;

/**
 * @author Thomas Freese
 */
class OracleJdbcDialect implements JdbcDialect {
    @Override
    public String getSelectSequenceNextValString(final String name) {
        return "select %s.nextval from dual".formatted(name);
    }

    @Override
    public String getSequenceNextValString(final String name) {
        return "%s.nextval".formatted(name);
    }
}
