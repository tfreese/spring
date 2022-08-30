package de.freese.jdbc.dialect;

/**
 * @author Thomas Freese
 */
class PostgresJdbcDialect implements JdbcDialect
{
    @Override
    public String dropSequence(final String name)
    {
        // CASCADE löscht auch alle referenzierenden Objekte.
        // RESTRICT löscht die Sequence nur, wenn es keine weiteren Abhängigkeiten gibt.
        return "DROP SEQUENCE %s RESTRICT".formatted(name);
    }

    @Override
    public String getSequenceNextValString(final String name)
    {
        return "nextval('%s')".formatted(name);
    }

    @Override
    public String getSelectSequenceNextValString(final String name)
    {
        return "select nextval('%s')".formatted(name);
    }
}
